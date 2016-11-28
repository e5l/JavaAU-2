package ru.spbau.mit.torrent.client;

import ru.spbau.mit.torrent.storage.BlockFile;
import ru.spbau.mit.utils.net.SocketServer;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Seeder extends SocketServer {
    private final Thread seederThread;
    private final ConcurrentHashMap<Integer, BlockFile> files;

    public Seeder(ConcurrentHashMap<Integer, BlockFile> files, int port) throws IOException {
        super(port);
        this.files = files;

        seederThread = new Thread(this);
        seederThread.start();
    }

    @Override
    protected void acceptClient(Socket client) {
        try {
            new Thread(new SeederHandler(client, files)).start();
        } catch (IOException e) {
        }
    }

    public void stop() {
        stopJobs();

        try {
            seederThread.join();
        } catch (InterruptedException e) {
        }
    }

    public short getPort() {
        return (short) socket.getLocalPort();
    }
}
