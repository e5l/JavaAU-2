package ru.spbau.mit.torrent.protocol.p2p;

import ru.spbau.mit.torrent.protocol.IMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class StatResponse implements IMessage {
    public final Set<Integer> parts;

    public StatResponse(Set<Integer> parts) {
        this.parts = parts;
    }

    @Override
    public void write(DataOutputStream output) throws IOException {
        output.writeInt(parts.size());
        for (int i : parts) {
            output.writeInt(i);
        }

        output.flush();
    }

    public static StatResponse readFrom(DataInputStream inputStream) throws IOException {
        final int count = inputStream.readInt();

        final Set<Integer> result = new HashSet<>();
        for (int i = 0; i < count; i++) {
            result.add(inputStream.readInt());
        }

        return new StatResponse(result);
    }
}
