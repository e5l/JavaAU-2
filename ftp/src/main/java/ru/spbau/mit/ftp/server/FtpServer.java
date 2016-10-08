package ru.spbau.mit.ftp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FtpServer implements Runnable {
    private final ServerSocket socket;

    public FtpServer(int port) throws IOException {
        socket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (true) {
            processConnection();
        }
    }

    private void processConnection() {
        try {
            final Socket connection = socket.accept();
            new Thread(new ClientHandler(connection)).start();
        } catch (Exception e) {
            System.out.println(String.format("Error happens: %s", e.getMessage()));
        }
    }
}
