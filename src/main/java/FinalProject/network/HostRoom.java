package FinalProject.network;

import java.io.*;
import java.net.*;

public class HostRoom {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    public void startServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("等待玩家連線...");
        clientSocket = serverSocket.accept();
        System.out.println("玩家已連線: " + clientSocket.getInetAddress());

        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void send(String message) {
        out.println(message);
    }

    public String receive() throws IOException {
        return in.readLine();
    }

    public void close() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
} 