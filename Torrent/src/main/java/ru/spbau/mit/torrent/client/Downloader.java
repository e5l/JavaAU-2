package ru.spbau.mit.torrent.client;

import ru.spbau.mit.torrent.client.storage.BlockFile;
import ru.spbau.mit.torrent.client.utils.OnDownload;
import ru.spbau.mit.torrent.protocol.P2PType;
import ru.spbau.mit.torrent.server.storage.SocketInfo;
import ru.spbau.mit.torrent.utils.Pair;

import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Downloader extends Thread {
    final ConcurrentLinkedQueue<Pair<BlockFile, List<SocketInfo>>> tasks = new ConcurrentLinkedQueue<>();
    private final OnDownload onDownload;
    volatile boolean closed = false;

    public Downloader(OnDownload onDownload) {
        this.onDownload = onDownload;
    }

    @Override
    public void run() {
        while (!closed) {
            final Pair<BlockFile, List<SocketInfo>> task = tasks.poll();
            if (task == null) {
                continue;
            }

            try {
                process(task.first, task.second);
            } catch (IOException e) {
                System.out.printf("Failed to download file %s: %s%n", task.first.toString(), e.getMessage());
            }
        }
    }

    private void process(BlockFile file, List<SocketInfo> seeds) throws IOException {
        final Iterator<SocketInfo> iterator = seeds.iterator();
        while (file.getRemainingBlocksSize() > 0 && iterator.hasNext() && !closed) {
            final SocketInfo seed = iterator.next();
            final Set<Integer> blocks = getBlocks(seed, file.id);
            final Set<Integer> remainingBlocks = file.getRemainingBlocks();

            blocks.retainAll(remainingBlocks);
            for (Integer block : blocks) {
                final byte[] data = downloadBlock(seed, file.id, block);
                file.writeBlock(block, data);

                if (closed) {
                    return;
                }
            }
        }

        if (file.getRemainingBlocksSize() == 0) {
            onDownload.onDownload(file);
        }
    }


    public synchronized void addTask(BlockFile file, List<SocketInfo> seedList) {
        tasks.add(new Pair<>(file, seedList));
    }

    public void close() {
        closed = true;
    }

    private Set<Integer> getBlocks(SocketInfo seed, Integer id) throws IOException {
        byte[] ip = seed.ip;
        try (Socket socket = new Socket(String.format("%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]), seed.port)) {
            final DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            final DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            outputStream.writeByte(P2PType.STAT.toByte());
            outputStream.writeInt(id);
            outputStream.flush();
            final int count = inputStream.readInt();

            final Set<Integer> result = new HashSet<>();
            for (int i = 0; i < count; i++) {
                result.add(inputStream.readInt());
            }
            return result;
        }
    }

    private byte[] downloadBlock(SocketInfo seed, Integer id, Integer block) throws IOException {
        byte[] ip = seed.ip;
        Socket socket = null;
        DataInputStream inputStream;
        DataOutputStream outputStream;
        try {
            socket = new Socket(String.format("%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]), seed.port);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            outputStream.writeByte(P2PType.GET.toByte());
            outputStream.writeInt(id);
            outputStream.writeInt(block);
            outputStream.flush();

            byte[] result = new byte[BlockFile.BLOCK_SIZE];
            inputStream.read(result);

            return result;
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
