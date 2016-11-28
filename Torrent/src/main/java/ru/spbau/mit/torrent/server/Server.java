package ru.spbau.mit.torrent.server;

import ru.spbau.mit.torrent.storage.Storage;
import ru.spbau.mit.utils.net.DataStreamHandler;
import ru.spbau.mit.utils.net.SocketServer;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;

public class Server extends SocketServer {
    private final LinkedList<ServerHandler> clients = new LinkedList<>();
    private final Thread current;
    private final Storage storage;
    private final String configPath;

    public Server(int port, String configPath) throws IOException {
        super(port);
        this.configPath = configPath;
        storage = loadOrCreate();

        current = new Thread(this);
        current.start();
    }

    @Override
    protected void acceptClient(Socket client) {
        try {
            ServerHandler handler = new ServerHandler(client, storage);
            new Thread(handler).start();
            clients.add(handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void stopJobs() {
        clients.forEach(DataStreamHandler::stopJobs);

        try {
            socket.close();
        } catch (IOException e) {
        }

        save();
    }

    public void stop() {
        stopJobs();
        try {
            current.join();
        } catch (InterruptedException e) {
        }
    }

    private Storage loadOrCreate() {
        final File file = new File(configPath, "server.storage");

        if (!file.exists()) {
            return new Storage();
        }

        try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file))) {
            return (Storage) stream.readObject();
        } catch (ClassNotFoundException e) {
            // Never happened
        } catch (IOException e) {
            System.out.printf("Server snapshot corruption: %s%n", e.getMessage());
        }

        return null;
    }

    private void save() {
        final File file = new File(configPath, "server.storage");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Couldn't create file: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file))) {
            objectOutputStream.writeObject(storage);
        } catch (IOException e) {
            System.out.println("Save error: " + e.getMessage());
        }
    }

}
