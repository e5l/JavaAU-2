package ru.spbau.mit.torrent.client.storage;

public class FileInfo {
    public final int id;
    public final long size;
    public final String name;

    public FileInfo(int id, long size, String name) {
        this.id = id;
        this.size = size;
        this.name = name;
    }
}
