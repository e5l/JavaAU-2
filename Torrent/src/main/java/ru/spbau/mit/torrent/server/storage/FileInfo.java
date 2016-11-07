package ru.spbau.mit.torrent.server.storage;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FileInfo {
    public final int id;
    public final String name;
    public final long size;
    
    private Set<ClientInfo> seeds = new HashSet<>();

    public FileInfo(int id, String name, long size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    @Override
    public int hashCode() {
        return id * 31 * 31 + 31 * ((int) size) + name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FileInfo &&
                ((FileInfo) o).name.equals(name) &&
                ((FileInfo) o).size == size;
    }

    public Set<ClientInfo> getActiveSeeds() {
        seeds = seeds.stream().filter(ClientInfo::isActive).collect(Collectors.toSet());
        return seeds;
    }

    public void addSeed(byte[] ip, int port) {
        seeds.add(new ClientInfo(new SocketInfo(ip, port)));
    }
}
