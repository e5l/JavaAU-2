package ru.spbau.mit.torrent.server;

import java.io.IOException;

public class ServerCLI {
    public static void main(String[] args) throws IOException, InterruptedException {
        final Server server = new Server(8081, System.getProperty("user.dir"));
        final Thread serverThread = new Thread(server);
        serverThread.start();

        System.in.read();
        server.stop();
        serverThread.join();
    }
}
