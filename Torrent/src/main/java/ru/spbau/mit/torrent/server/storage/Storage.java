package ru.spbau.mit.torrent.server.storage;

import java.io.Serializable;
import java.util.*;

public class Storage implements Serializable {
    private final HashMap<SocketInfo, ClientInfo> users = new HashMap<>();
    private final ArrayList<FileInfo> index = new ArrayList<>();

    public synchronized List<FileInfo> list() {
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

    public synchronized boolean update(byte[] ip, int port, ArrayList<Integer> files) {
        SocketInfo info = new SocketInfo(ip, port);
        if (users.containsKey(info)) {
            users.get(info).update();
            return true;
        }

        ClientInfo clientInfo = new ClientInfo(new int[]{}, info);
        users.put(info, clientInfo);
        return true;
    }
}
