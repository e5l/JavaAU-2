package ru.spbau.mit.utils.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DataStreamClient {
    protected final DataInputStream inputStream;
    protected final DataOutputStream outputStream;
    protected final Socket socket;

    public DataStreamClient(Socket socket) throws IOException {
        this.socket = socket;
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
    }

    protected void stopJobs() {
        try {
            outputStream.close();
        } catch (IOException e) {
        }

        try {
            inputStream.close();
        } catch (IOException e) {
        }

        try {
            socket.close();
        } catch (IOException e) {
        }
    }

}
