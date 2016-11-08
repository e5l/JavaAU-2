package ru.spbau.mit.torrent.protocol.p2p;

import ru.spbau.mit.torrent.protocol.IMessage;
import ru.spbau.mit.torrent.protocol.P2PType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DownloadRequest implements IMessage {
    public final Integer id;
    public final Integer block;

    public DownloadRequest(Integer id, Integer block) {
        this.id = id;
        this.block = block;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeByte(P2PType.GET.toByte());
        output.writeInt(id);
        output.writeInt(block);
        output.flush();
    }

    public static DownloadRequest readFrom(DataInputStream inputStream) throws IOException {
        return new DownloadRequest(inputStream.readInt(), inputStream.readInt());
    }
}
