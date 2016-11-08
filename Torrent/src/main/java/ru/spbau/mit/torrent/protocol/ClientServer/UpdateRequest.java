package ru.spbau.mit.torrent.protocol.ClientServer;

import ru.spbau.mit.torrent.protocol.ClientType;
import ru.spbau.mit.torrent.protocol.IMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UpdateRequest implements IMessage {
    public final short port;
    public final Set<Integer> files;

    public UpdateRequest(short port, Set<Integer> files) {
        this.port = port;
        this.files = files;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeByte(ClientType.UPDATE.toByte());
        output.writeShort(port);
        output.writeInt(files.size());

        for (int key : files) {
            output.writeInt(key);
        }

        output.flush();
    }

    public static UpdateRequest readFrom(DataInputStream inputStream) throws IOException {
        final short port = inputStream.readShort();
        final int filesCount = inputStream.readInt();

        final Set<Integer> files = new HashSet<>();
        for (int i = 0; i < filesCount; i++) {
            files.add(inputStream.readInt());
        }

        return new UpdateRequest(port, files);
    }
}
