package ru.spbau.mit.torrent.client;

import ru.spbau.mit.torrent.client.storage.BlockFile;
import ru.spbau.mit.torrent.protocol.P2PType;
import ru.spbau.mit.utils.net.DataStreamHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SeederHandler extends DataStreamHandler {
    private final ConcurrentHashMap<Integer, BlockFile> files;

    public SeederHandler(Socket client, ConcurrentHashMap<Integer, BlockFile> files) throws IOException {
        super(client);
        this.files = files;
    }

    @Override
    protected void processCommand() throws IOException {
        final P2PType type = P2PType.fromByte(inputStream.readByte());
        switch (type) {
            case STAT:
                stat();
                break;
            case GET:
                get();
                break;
        }
    }

    private void stat() throws IOException {
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

    private void get() throws IOException  {
        final int id = inputStream.readInt();
        final int part = inputStream.readInt();

        if (!files.containsKey(id)) {
            throw new FileNotFoundException();
        }

        final BlockFile blockFile = files.get(id);
        if (blockFile.getRemainingBlocks().contains(part)) {
            // TODO: log error
            return;
        }

        byte[] bytes = blockFile.readBlock(part);
        outputStream.write(bytes);
        outputStream.flush();
    }
}
