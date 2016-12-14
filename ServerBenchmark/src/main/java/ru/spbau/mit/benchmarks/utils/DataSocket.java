package ru.spbau.mit.benchmarks.utils;

import com.google.protobuf.GeneratedMessageV3;

import java.io.*;
import java.net.Socket;

public final class DataSocket implements AutoCloseable {
    private final DataOutputStream out;
    private final Socket socket;
    private final DataInputStream in;

    public DataSocket(final Socket socket) throws IOException {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        this.socket = socket;
    }

    public void write(GeneratedMessageV3 message) throws IOException {
        int size = message.getSerializedSize();
        byte[] data = message.toByteArray();

        out.writeInt(size);
        out.write(data);
        out.flush();
    }

    public byte[] read() throws IOException {
        int size = in.readInt();
        byte[] data = new byte[size];

        in.readFully(data);
        return data;
    }

    public Pair<Long, byte[]> readAndMeasure() throws IOException {
        int size = in.readInt();
        Long time = System.currentTimeMillis();

        byte[] data = new byte[size];
        in.readFully(data);
        return new Pair<>(time, data);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
