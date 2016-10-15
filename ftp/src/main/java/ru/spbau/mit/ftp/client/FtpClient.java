package ru.spbau.mit.ftp.client;

import org.apache.commons.lang3.tuple.ImmutablePair;
import ru.spbau.mit.ftp.utils.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FtpClient {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public FtpClient(String host, int port) throws IOException {
        socket = new Socket(host, port);

        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public byte[] readFile(String path) throws IOException {
        outputStream.writeInt(Command.GET.id);
        outputStream.writeUTF(path);
        outputStream.flush();

        final int size = inputStream.readInt();
        byte[] data = new byte[size];
        int read = 0;
        while (read < size) {
            read += inputStream.read(data, read, size - read);
        }

        return data;
    }

    public List<ImmutablePair<String, Boolean>> listDirectory(String path) throws IOException {
        outputStream.writeInt(Command.LIST.id);
        outputStream.writeUTF(path);
        outputStream.flush();

        List<ImmutablePair<String, Boolean>> result = new ArrayList<>();
        final int size = inputStream.readInt();

        for (int i = 0; i < size; i++) {
            String name = inputStream.readUTF();
            Boolean isDirectory = inputStream.readBoolean();

            result.add(new ImmutablePair<>(name, isDirectory));
        }

        return result;
    }

    public void close() throws IOException {
        outputStream.writeInt(Command.DISCONNECT.id);
        outputStream.flush();

        if (!socket.isClosed()) {
            socket.close();
        }
    }
}
