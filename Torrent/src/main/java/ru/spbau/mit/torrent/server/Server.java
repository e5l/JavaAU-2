package ru.spbau.mit.torrent.server;

import ru.spbau.mit.torrent.server.storage.Storage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

/**
 * TODO: удаление клиентов по таймаутfileу
 */
public class Server extends Thread {
    private final int port;
    private ServerSocket socket;
    private final LinkedList<ServerHandler> clients = new LinkedList<>();
    private final Storage storage;

    public Server(int port) {
        this.port = port;
        storage = loadOrCreate();
    }

    private Storage loadOrCreate() {
        final String path = System.getProperty("user.dir");
        final File file = new File(path, "server.storage");

        if (!file.exists()) {
            return new Storage();
        }

        try {
            final ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
            Storage result = (Storage) stream.readObject();
            stream.close();
            return result;
        } catch (IOException e) {
            System.out.println("Failed to read storage state: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void save() {
        final String path = System.getProperty("user.dir");
        final File file = new File(path, "server.storage");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Couldn't create file: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeObject(storage);
            objectOutputStream.close();
        } catch (IOException e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

    public void run() {
        try {
            socket = new ServerSocket(port);
            while (!socket.isClosed()) {
                final Socket connection = socket.accept();
                ServerHandler client = new ServerHandler(connection, storage);
                new Thread(client).start();
                clients.add(client);
            }
        } catch (IOException e) {
            System.out.println("SocketInfo: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        for (ServerHandler client : clients) {
            client.close();
        }

        socket.close();
        save();
    }
}
