package ru.spbau.mit.benchmarks.utils;

import com.google.protobuf.GeneratedMessageV3;

import java.io.*;
import java.net.Socket;

public final class DataSocket {
    private final DataOutputStream out;
    private final DataInputStream in;

    public DataSocket(final Socket socket) throws IOException {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
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
}
