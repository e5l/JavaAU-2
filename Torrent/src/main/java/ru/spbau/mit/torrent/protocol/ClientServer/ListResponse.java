package ru.spbau.mit.torrent.protocol.ClientServer;

import ru.spbau.mit.torrent.protocol.IMessage;
import ru.spbau.mit.torrent.server.storage.FileInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ListResponse implements IMessage {
    public final List<FileInfo> response;

    public ListResponse(List<FileInfo> response) {
        this.response = response;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeInt(response.size());
        for (final FileInfo info : response) {
            output.writeInt(info.id);
            output.writeUTF(info.name);
            output.writeLong(info.size);
        }

        output.flush();
    }

    public static ListResponse readFrom(DataInputStream inputStream) throws IOException {
        final int size = inputStream.readInt();
        List<FileInfo> response = new LinkedList<>();

        for (int i = 0; i < size; i++) {
            final int id = inputStream.readInt();
            final String name = inputStream.readUTF();
            final long fileSize = inputStream.readLong();

            final FileInfo info = new FileInfo(id, name, fileSize);
            response.add(info);
        }

        return new ListResponse(response);
    }
}
