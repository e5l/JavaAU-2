package ru.spbau.mit.torrent.client;

import ru.spbau.mit.torrent.client.storage.BlockFile;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Seeder implements Runnable {
    private final ConcurrentHashMap<Integer, BlockFile> files;
    private final ServerSocket socket;

    public Seeder(ConcurrentHashMap<Integer, BlockFile> files, int port) throws IOException {
        this.files = files;
        socket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                final Socket client = socket.accept();
                new Thread(new SeederHandler(client, files)).start();
            } catch (IOException e) {
                System.out.println("Failed to process client");
            }
        }
    }

    public void close() throws IOException {
        socket.close();
    }
}
