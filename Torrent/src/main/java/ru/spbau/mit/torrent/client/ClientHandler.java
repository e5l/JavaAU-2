package ru.spbau.mit.torrent.client;

import ru.spbau.mit.torrent.client.exceptions.PartNotFoundException;
import ru.spbau.mit.torrent.client.storage.BlockFile;
import ru.spbau.mit.torrent.protocol.P2PType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private final Socket client;
    final DataInputStream inputStream;
    final DataOutputStream outputStream;
    private final ConcurrentHashMap<Integer, BlockFile> files;

    public ClientHandler(Socket client, ConcurrentHashMap<Integer, BlockFile> files) throws IOException {
        this.client = client;
        this.files = files;
        inputStream = new DataInputStream(client.getInputStream());
        outputStream = new DataOutputStream(client.getOutputStream());
    }

    @Override
    public void run() {
        while (!client.isClosed()) {
            try {
                final P2PType type = P2PType.fromByte(inputStream.readByte());
                switch (type) {
                    case STAT:
                        stat();
                        break;
                    case GET:
                        get();
                        break;
                }

            } catch (FileNotFoundException e) {
                System.out.println("Client request missing file");
            } catch (PartNotFoundException e) {
                System.out.println("Client request missing part");
            } catch (IOException e) {
                System.out.printf("Couldn't process client request: %s%n", e.getMessage());
            }
        }

    }

    public void close() throws IOException {
        client.close();
    }

    private void stat() throws IOException, PartNotFoundException {
        final int id = inputStream.readInt();

        if (!files.containsKey(id)) {
            throw new FileNotFoundException();
        }

        final BlockFile blockFile = files.get(id);
        final int totalSize = blockFile.getBlocksCount();
        final int count = totalSize - blockFile.getRemainingBlocksSize();
        final Set<Integer> remainingBlocks = blockFile.getRemainingBlocks();

        outputStream.writeInt(count);
        for (int i = 0; i < totalSize; ++i) {
            if (remainingBlocks.contains(i)) {
                continue;
            }

            outputStream.writeInt(i);
        }

        outputStream.flush();
    }

    private void get() throws IOException, PartNotFoundException {
        final int id = inputStream.readInt();
        final int part = inputStream.readInt();

        if (!files.containsKey(id)) {
            throw new FileNotFoundException();
        }

        final BlockFile blockFile = files.get(id);
        if (!blockFile.getRemainingBlocks().contains(part)) {
            throw new PartNotFoundException();
        }

        byte[] bytes = blockFile.readBlock(part);
        outputStream.write(bytes);
        outputStream.flush();
    }
}
