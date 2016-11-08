package ru.spbau.mit.utils.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class SocketServer implements Runnable {
    private final Logger logger = LoggerFactory.getLogger("Socket server");

    private final int port;
    protected ServerSocket socket;

    protected SocketServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            initSocket();
            while (!socket.isClosed()) {
                final Socket client = socket.accept();
                acceptClient(client);
            }
        } catch (IOException e) {
            logger.debug("Eval loop failed %s", e.getMessage());
        } finally {
            stopJobs();
        }
    }

    protected void initSocket() throws IOException {
        socket = new ServerSocket(port);
    }

    protected void stopJobs() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.debug("Failed to stop server %s", e.getMessage());
        }
    }

    protected abstract void acceptClient(Socket client);
}
