package ru.spbau.mit.torrent.client.storage;

import java.io.Serializable;

public class FileInfo implements Serializable {
    public final int id;
    public final long size;
    public final String name;

    public FileInfo(int id, long size, String name) {
        this.id = id;
        this.size = size;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FileInfo)) {
            return false;
        }

        FileInfo instance = (FileInfo) o;
        return instance.id == id &&
                instance.size == size &&
                instance.name.equals(name);
    }

}
