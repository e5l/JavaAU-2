package ru.spbau.mit.torrent.protocol.ClientServer;

import ru.spbau.mit.torrent.protocol.ClientType;
import ru.spbau.mit.torrent.protocol.IMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UploadRequest implements IMessage {
    public final String name;
    public final long size;

    public UploadRequest(String name, long size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeByte(ClientType.UPLOAD.toByte());
        output.writeUTF(name);
        output.writeLong(size);
        output.flush();
    }

    public static UploadRequest readFrom(DataInputStream input) throws IOException {
        return new UploadRequest(input.readUTF(), input.readLong());
    }

}
