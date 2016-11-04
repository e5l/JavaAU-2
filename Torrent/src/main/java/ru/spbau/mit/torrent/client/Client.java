package ru.spbau.mit.torrent.client;

import ru.spbau.mit.torrent.client.storage.BlockFile;
import ru.spbau.mit.torrent.client.storage.FileInfo;
import ru.spbau.mit.torrent.client.utils.OnDownload;
import ru.spbau.mit.torrent.protocol.ClientType;
import ru.spbau.mit.torrent.server.storage.SocketInfo;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
1. updater
 */

public class Client {
    private static final String CLIENT_CONFIG_PATH = "client.config";

    private final Socket serverConnection;

    private final DataOutputStream outputStream;
    private final DataInputStream inputStream;

    private final Seeder seeder;
    private final Downloader downloader;

    private ConcurrentHashMap<Integer, BlockFile> files = new ConcurrentHashMap<>();
    private final HashMap<Integer, FileInfo> catalog = new HashMap<>();

    public Client(short port, String serverIp, short serverPort, OnDownload onDownload) throws IOException, ClassNotFoundException {
        serverConnection = new Socket(serverIp, serverPort);
        outputStream = new DataOutputStream(serverConnection.getOutputStream());
        inputStream = new DataInputStream(serverConnection.getInputStream());

        load();

        seeder = new Seeder(files, port);
        downloader = new Downloader(onDownload);
        new Thread(seeder).start();
        downloader.start();

        files.entrySet().stream().filter(it -> it.getValue().getRemainingBlocksSize() > 0).forEach(it -> {
            try {
                List<SocketInfo> seedsList = getSeedsList(it.getKey());
                downloader.addTask(it.getValue(), seedsList);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public HashMap<Integer, FileInfo> listFiles() throws IOException {
        outputStream.writeByte(ClientType.LIST.toByte());
        outputStream.flush();

        final int size = inputStream.readInt();
        catalog.clear();
        for (int i = 0; i < size; i++) {
            final int id = inputStream.readInt();
            final String name = inputStream.readUTF();
            final long fileSize = inputStream.readLong();

            final FileInfo info = new FileInfo(id, fileSize, name);
            catalog.put(id, info);
        }

        return catalog;
    }

    public void upload(String path) throws IOException {
        final File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException(path);
        }

        outputStream.writeByte(ClientType.UPLOAD.toByte());
        outputStream.writeUTF(file.getName());
        outputStream.writeLong(file.length());
        outputStream.flush();

        final int id = inputStream.readInt();
        files.put(id, new BlockFile(file, file.length(), id));
    }

    public void download(int id, String destination) throws IOException {
        if (!catalog.containsKey(id)) {
            throw new IndexOutOfBoundsException();
        }

        List<SocketInfo> seedList = getSeedsList(id);
        BlockFile file = new BlockFile(new File(destination), catalog.get(id).size, id);
        files.put(id, file);

        downloader.addTask(file, seedList);
    }

    public void stop() {
        try {
            downloader.close();
            downloader.join();
        } catch (InterruptedException e) {
            System.out.printf("Failed to stop downloader: %s%n", e.getMessage());
        }

        try {
            seeder.close();
        } catch (IOException e) {
            System.out.printf("Failed to stop seeding: %s%n", e.getMessage());
        }

        try {
            serverConnection.close();
        } catch (IOException e) {
            System.out.printf("Failed to close server connection: %s%n", e.getMessage());
        }

        try {
            save();
        } catch (IOException e) {
            System.out.printf("Failed to save client state:%s%n", e.getMessage());
        }
    }

    private void load() throws IOException, ClassNotFoundException {
        final File file = new File(CLIENT_CONFIG_PATH);
        if (!file.exists() || file.isDirectory()) {
            return;
        }

        ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
        files = (ConcurrentHashMap<Integer, BlockFile>) stream.readObject();
        stream.close();
    }

    private void save() throws IOException {
        final File file = new File(CLIENT_CONFIG_PATH);
        if (!file.exists()) {
            file.createNewFile();
        }

        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
        stream.writeObject(files);
        stream.close();
    }

    private List<SocketInfo> getSeedsList(int id) throws IOException {
        outputStream.writeByte(ClientType.SOURCES.toByte());
        outputStream.writeInt(id);
        outputStream.flush();

        int size = inputStream.readInt();
        List<SocketInfo> clients = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            byte[] ip = new byte[4];
            for (int j = 0; j < 4; j++) {
                ip[j] = inputStream.readByte();
            }

            int port = inputStream.readShort();
            clients.add(new SocketInfo(ip, port));
        }

        return clients;
    }
}
