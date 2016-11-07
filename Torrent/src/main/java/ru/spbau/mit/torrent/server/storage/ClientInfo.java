package ru.spbau.mit.torrent.server.storage;

import java.util.Date;

public class ClientInfo {
    public final long LIVE_TIME = 5 * 60 * 1000;
    public final SocketInfo socket;

    private Date update_time;

    public ClientInfo(SocketInfo socket) {
        update_time = new Date();

        this.socket = socket;
    }

    public void update() {
        update_time = new Date();
    }

    public boolean isActive() {
        long diff = (new Date()).getTime() - update_time.getTime();
        return (diff <= LIVE_TIME);
    }
}
