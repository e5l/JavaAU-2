package ru.spbau.mit.torrent.server.storage;

import java.util.Arrays;

public class SocketInfo {
    public final byte[] ip;
    public final int port;

    public SocketInfo(byte[] ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ip) * 31 + port;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SocketInfo &&
                Arrays.equals(ip, ((SocketInfo) o).ip) &&
                port == ((SocketInfo) o).port;
    }
}
