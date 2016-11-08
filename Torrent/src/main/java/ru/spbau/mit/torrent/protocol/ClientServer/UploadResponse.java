package ru.spbau.mit.torrent.protocol.ClientServer;

import ru.spbau.mit.torrent.protocol.IMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UploadResponse implements IMessage {
    public final int id;

    public UploadResponse(int id) {
        this.id = id;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeInt(id);
        output.flush();
    }

    public static UploadResponse readFrom(DataInputStream inputStream) throws IOException {
        return new UploadResponse(inputStream.readInt());
    }
}
