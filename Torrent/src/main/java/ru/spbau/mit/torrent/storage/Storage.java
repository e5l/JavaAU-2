package ru.spbau.mit.torrent.storage;

import java.io.Serializable;
import java.util.*;

public class Storage implements Serializable {
    private final Map<SocketInfo, ClientInfo> users = new HashMap<>();
    private final ArrayList<FileInfo> index = new ArrayList<>();

    public synchronized List<FileInfo> list() {
        //noinspection unchecked
        return (ArrayList<FileInfo>) index.clone();
    }

    public synchronized int upload(String name, long size) {
        index.add(new FileInfo(index.size(), name, size));
        return index.size() - 1;
    }

    public synchronized Set<ClientInfo> sources(int id) {
        Set<ClientInfo> seeds = new HashSet<>();
        seeds.addAll(index.get(id).getActiveSeeds());

        return seeds;
    }

    public synchronized boolean update(byte[] ip, int port, Set<Integer> files) {
        SocketInfo info = new SocketInfo(ip, port);
        if (users.containsKey(info)) {
            users.get(info).update();
        }

        for (int file : files) {
            index.get(file).addSeed(ip, port);
        }

        ClientInfo clientInfo = new ClientInfo(info);
        users.put(info, clientInfo);
        return true;
    }
}
