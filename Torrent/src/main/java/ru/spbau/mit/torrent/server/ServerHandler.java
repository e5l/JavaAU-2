package ru.spbau.mit.torrent.server;

import ru.spbau.mit.torrent.protocol.ClientType;
import ru.spbau.mit.torrent.server.storage.ClientInfo;
import ru.spbau.mit.torrent.server.storage.FileInfo;
import ru.spbau.mit.torrent.server.storage.Storage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServerHandler implements Runnable {
    private final Socket connection;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final Storage storage;

    public ServerHandler(Socket connection, Storage storage) throws IOException {
        this.connection = connection;
        this.storage = storage;
        inputStream = new DataInputStream(connection.getInputStream());
        outputStream = new DataOutputStream(connection.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (!connection.isClosed()) {
                evalCommand();
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected");
        } catch (IOException e) {
            System.out.println("Failed to execute command: " + e.getMessage());
        }

        try {
            connection.close();
        } catch (IOException e) {
            System.out.println("Failed to close connection: " + e.getMessage());
        }
    }

    public void close() throws IOException {
        connection.close();
    }

    private void evalCommand() throws IOException {
        final ClientType type = ClientType.fromByte(inputStream.readByte());

        switch (type) {
            case LIST:
                list();
                break;
            case UPLOAD:
                upload();
                break;
            case SOURCES:
                sources();
                break;
            case UPDATE:
                update();
                break;
        }
    }

    private void list() throws IOException {
        final List<FileInfo> response = storage.list();

        outputStream.writeInt(response.size());
        for (final FileInfo info : response) {
            outputStream.writeInt(info.id);
            outputStream.writeUTF(info.name);
            outputStream.writeLong(info.size);
        }

        outputStream.flush();
    }

    private void upload() throws IOException {
        final String name = inputStream.readUTF();
        final long size = inputStream.readLong();

        final int id = storage.upload(name, size);
        outputStream.writeInt(id);
        outputStream.flush();
    }

    private void sources() throws IOException {
        final int id = inputStream.readInt();
        final Set<ClientInfo> sources = storage.sources(id);

        outputStream.writeInt(sources.size());
        for (ClientInfo info : sources) {
            for (int i = 0; i < 4; i++) {
                outputStream.writeByte(info.socket.ip[i]);
            }

            outputStream.writeShort(info.socket.port);
        }

        outputStream.flush();
    }

    private void update() throws IOException {
        final short port = inputStream.readShort();
        final int filesCount = inputStream.readInt();

        final ArrayList<Integer> files = new ArrayList<>();
        for (int i = 0; i < filesCount; i++) {
            files.add(inputStream.readInt());
        }

        final boolean status = storage.update(connection.getInetAddress().getAddress(), port, files);
        outputStream.writeBoolean(status);
    }

}
