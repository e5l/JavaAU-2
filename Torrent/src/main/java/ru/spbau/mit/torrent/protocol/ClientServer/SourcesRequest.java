package ru.spbau.mit.torrent.protocol.ClientServer;

import ru.spbau.mit.torrent.protocol.ClientType;
import ru.spbau.mit.torrent.protocol.IMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SourcesRequest implements IMessage {
    public final int id;

    public SourcesRequest(int id) {
        this.id = id;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeByte(ClientType.SOURCES.toByte());
        output.writeInt(id);
        output.flush();
    }


    public static SourcesRequest readFrom(DataInputStream inputStream) throws IOException {
        return new SourcesRequest(inputStream.readInt());
    }

}
