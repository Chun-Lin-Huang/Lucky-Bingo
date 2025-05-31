package FinalProject.network;

import java.io.*;
import java.net.*;

public class JoinRoom {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public void connectToHost(String host, int port) throws IOException {
        socket = new Socket(host, port);
        System.out.println("已連線至主機: " + host);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
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
        socket.close();
    }
} 