package ru.spbau.mit.ftp.server;

import org.apache.commons.lang3.tuple.ImmutablePair;
import ru.spbau.mit.ftp.server.exception.MustBeDirectoryException;
import ru.spbau.mit.ftp.server.fs.Browser;
import ru.spbau.mit.ftp.utils.Command;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final Browser browser;

    public ClientHandler(Socket socket) throws IOException, MustBeDirectoryException {
        this.socket = socket;

        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        browser = new Browser(System.getProperty("user.dir"));
    }

    @Override
    public void run() {
        boolean result = true;
        while (result) {
            result = processCommand();
        }

        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private boolean processCommand() {
        try {
            final Command command = Command.fromInt(inputStream.readInt());
            switch (command) {
                case DISCONNECT:
                    return false;
                case LIST:
                    listFolder();
                    break;
                case GET:
                    getFile();
                    break;
            }
        } catch (Exception exception) {
            System.out.println(String.format("Client query invalid: %s", exception.getMessage()));
            return false;
        }

        return true;
    }

    private void listFolder() throws IOException {
        final String path = inputStream.readUTF();
        final List<ImmutablePair<String, Boolean>> result = browser.listDirectory(path);

        outputStream.writeInt(result.size());
        for (ImmutablePair<String, Boolean> it : result) {
            outputStream.writeUTF(it.getLeft());
            outputStream.writeBoolean(it.getRight());
        }

        outputStream.flush();
    }

    private void getFile() throws IOException {
        final String path = inputStream.readUTF();
        List<Byte> bytes;

        try {
            bytes = browser.readFile(path);
        } catch (IOException error) {
            outputStream.writeInt(0);
            return;
        }

        outputStream.writeInt(bytes.size());
        for (Byte it : bytes) {
            outputStream.writeByte(it);
        }

        outputStream.flush();
    }

}
