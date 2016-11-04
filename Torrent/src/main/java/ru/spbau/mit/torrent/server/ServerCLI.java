package ru.spbau.mit.torrent.server;

import java.io.IOException;

public class ServerCLI {
    public static void main(String[] args) throws IOException {
        final Server server = new Server(8081);
        server.start();

        System.in.read();
        server.close();
    }
}
