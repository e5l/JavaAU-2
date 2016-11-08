package ru.spbau.mit.torrent.client;

import ru.spbau.mit.torrent.storage.BlockFile;
import ru.spbau.mit.torrent.protocol.P2PType;
import ru.spbau.mit.torrent.protocol.p2p.DownloadRequest;
import ru.spbau.mit.torrent.protocol.p2p.DownloadResponse;
import ru.spbau.mit.torrent.protocol.p2p.StatRequest;
import ru.spbau.mit.torrent.protocol.p2p.StatResponse;
import ru.spbau.mit.utils.net.DataStreamHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
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
        final int id = StatRequest.readFrom(inputStream).id;

        if (!files.containsKey(id)) {
            throw new FileNotFoundException();
        }

        final BlockFile blockFile = files.get(id);
        final int totalSize = blockFile.getBlocksCount();
        final Set<Integer> remainingBlocks = blockFile.getRemainingBlocks();
        final Set<Integer> result = new HashSet<>();

        for (int i = 0; i < totalSize; ++i) {
            if (remainingBlocks.contains(i)) {
                continue;
            }

            result.add(i);
        }

        new StatResponse(result).write(outputStream);
    }

    private void get() throws IOException  {
        final DownloadRequest request = DownloadRequest.readFrom(inputStream);

        if (!files.containsKey(request.id)) {
            throw new FileNotFoundException();
        }

        final BlockFile blockFile = files.get(request.id);
        if (blockFile.getRemainingBlocks().contains((request.block))) {
            // TODO: log error
            return;
        }

        byte[] bytes = blockFile.readBlock(request.block);

        new DownloadResponse(bytes).write(outputStream);
    }
}
