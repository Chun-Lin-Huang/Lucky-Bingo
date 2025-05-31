package FinalProject.network;

import java.io.IOException;

public class NetworkGameManager {
    private final boolean isHost;
    private final HostRoom hostRoom;
    private final JoinRoom joinRoom;

    public NetworkGameManager(boolean isHost) {
        this.isHost = isHost;
        this.hostRoom = isHost ? new HostRoom() : null;
        this.joinRoom = isHost ? null : new JoinRoom();
    }

    /**
     * Host: ipOrPort = "9999" (port)
     * Join: ipOrPort = "127.0.0.1:9999" (IP:Port)
     */
    public void startConnection(String ipOrPort) throws IOException {
        if (isHost) {
            int port = Integer.parseInt(ipOrPort);
            hostRoom.startServer(port);
        } else {
            String[] parts = ipOrPort.split(":");
            String ip = parts[0];
            int port = Integer.parseInt(parts[1]);
            joinRoom.connectToHost(ip, port);
        }
    }

    public void sendMessage(String msg) {
        if (isHost) {
            hostRoom.send(msg);
        } else {
            joinRoom.send(msg);
        }
    }

    public String receiveMessage() throws IOException {
        if (isHost) {
            return hostRoom.receive();
        } else {
            return joinRoom.receive();
        }
    }

    public void close() throws IOException {
        if (isHost) {
            hostRoom.close();
        } else {
            joinRoom.close();
        }
    }
}