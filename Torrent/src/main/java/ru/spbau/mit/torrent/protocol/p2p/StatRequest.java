package ru.spbau.mit.torrent.protocol.p2p;

import ru.spbau.mit.torrent.protocol.IMessage;
import ru.spbau.mit.torrent.protocol.P2PType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StatRequest implements IMessage {
    public final int id;

    public StatRequest(int id) {
        this.id = id;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeByte(P2PType.STAT.toByte());
        output.writeInt(id);
        output.flush();
    }

    public static StatRequest readFrom(DataInputStream inputStream) throws IOException {
        return new StatRequest(inputStream.readInt());
    }
}
