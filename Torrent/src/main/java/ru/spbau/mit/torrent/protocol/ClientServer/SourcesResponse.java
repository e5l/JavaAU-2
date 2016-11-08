package ru.spbau.mit.torrent.protocol.ClientServer;

import ru.spbau.mit.torrent.protocol.IMessage;
import ru.spbau.mit.torrent.storage.SocketInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SourcesResponse implements IMessage {
    public final Set<SocketInfo> sources;

    public SourcesResponse(Set<SocketInfo> sources) {
        this.sources = sources;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeInt(sources.size());
        for (SocketInfo info : sources) {
            for (int i = 0; i < 4; i++) {
                output.writeByte(info.ip[i]);
            }

            output.writeShort(info.port);
        }

        output.flush();
    }

    public static SourcesResponse readFrom(DataInputStream inputStream) throws IOException {
        int size = inputStream.readInt();

        Set<SocketInfo> clients = new HashSet<>();
        for (int i = 0; i < size; i++) {
            byte[] ip = new byte[4];
            for (int j = 0; j < 4; j++) {
                ip[j] = inputStream.readByte();
            }

            int port = inputStream.readShort();
            clients.add(new SocketInfo(ip, port));
        }

        return new SourcesResponse(clients);
    }
}
