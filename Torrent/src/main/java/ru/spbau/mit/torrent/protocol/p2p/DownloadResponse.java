package ru.spbau.mit.torrent.protocol.p2p;

import ru.spbau.mit.torrent.storage.BlockFile;
import ru.spbau.mit.torrent.protocol.IMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DownloadResponse implements IMessage {
    public final byte[] block;

    public DownloadResponse(byte[] block) {
        this.block = block;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.write(block);
        output.flush();
    }

    public static DownloadResponse readFrom(DataInputStream inputStream) throws IOException {
        byte[] result = new byte[BlockFile.BLOCK_SIZE];
        inputStream.read(result);
        return new DownloadResponse(result);
    }
}
