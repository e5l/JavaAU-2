package ru.spbau.mit.benchmarks.server.impl;

import ru.spbau.mit.benchmarks.utils.DataSocket;

public final class TcpThreadServer extends TcpBlockingProcessor {

    public TcpThreadServer(final int port) {
        super(port);
    }

    @Override
    protected void processClient(DataSocket client) {
        new Thread(getAllQueryExecutor(client)).start();
    }
}
