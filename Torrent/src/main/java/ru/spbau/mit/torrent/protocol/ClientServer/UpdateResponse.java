package ru.spbau.mit.torrent.protocol.ClientServer;

import ru.spbau.mit.torrent.protocol.IMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UpdateResponse implements IMessage{
    public final boolean status;

    public UpdateResponse(boolean status) {
        this.status = status;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeBoolean(status);
        output.flush();
    }

    public static UpdateResponse readFrom(DataInputStream inputStream) throws IOException {
        return new UpdateResponse(inputStream.readBoolean());
    }
}
