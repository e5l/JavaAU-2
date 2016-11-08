package ru.spbau.mit.utils.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class DataStreamHandler implements Runnable {
    protected final DataOutputStream outputStream;
    protected final DataInputStream inputStream;
    protected final Socket socket;

    public DataStreamHandler(Socket socket) throws IOException {
        this.socket = socket;
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputStream = new DataInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                processCommand();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            // TODO
        } finally {
            stopJobs();
        }
    }

    public void stopJobs() {
        try {
            inputStream.close();
        } catch (IOException e) {
            // TODO
            System.out.println(e.getMessage());
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            // TODO
            System.out.println(e.getMessage());
        }

        try {
            socket.close();
        } catch (IOException e) {
            // TODO
            System.out.println(e.getMessage());
        }

        onStop();
    }

    protected void processCommand() throws IOException {}
    protected void onStop() {}
}
