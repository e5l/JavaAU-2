package ru.spbau.mit.torrent.storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


public class BlockFile implements Serializable {
    public static final int BLOCK_SIZE = 1024;

    private final int id;
    private final File file;
    private final int blocksCount;
    private final HashSet<Integer> remainingBlocks = new HashSet<>();

    public BlockFile(File file, long length, int id) throws IOException {
        this.file = file;
        this.id = id;
        this.blocksCount = (int) Math.ceil(1.0 * length / BLOCK_SIZE);
        if (!file.exists()) {
            for (int i = 0; i < blocksCount; i++) {
                remainingBlocks.add(i);
            }
        }
        RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        raFile.setLength(length);
    }

    public synchronized boolean isAvailable(int block) {
        return !remainingBlocks.contains(block);
    }

    public synchronized byte[] readBlock(int id) throws IOException {
        final int size = getBlockSize(id);
        final byte[] result = new byte[BLOCK_SIZE];
        final int offset = id * BLOCK_SIZE;

        final RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        raFile.seek(offset);
        raFile.read(result, 0, size);
        return result;
    }

    public synchronized void writeBlock(int id, byte[] block) throws IOException {
        final int size = getBlockSize(id);
        final int offset = id * BLOCK_SIZE;

        final RandomAccessFile raFile = new RandomAccessFile(file, "rw");
        raFile.seek(offset);
        raFile.write(block, 0, size);
        remainingBlocks.remove(id);
    }

    public int getBlocksCount() {
        return blocksCount;
    }

    public synchronized int getRemainingBlocksSize() {
        return remainingBlocks.size();
    }

    public synchronized Set<Integer> getRemainingBlocks() {
        final HashSet<Integer> result = new HashSet<>();
        result.addAll(remainingBlocks);
        return result;
    }

    private int getBlockSize(int id) throws IOException {
        if (id != blocksCount - 1) {
            return BLOCK_SIZE;
        }

        return (int) (file.length() - (blocksCount - 1) * BLOCK_SIZE);
    }

    @Override
    public String toString() {
        return file.getName();
    }

    public int getId() {
        return id;
    }
}
