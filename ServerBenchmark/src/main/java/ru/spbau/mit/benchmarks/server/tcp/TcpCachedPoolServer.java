package ru.spbau.mit.benchmarks.server.tcp;

import ru.spbau.mit.benchmarks.utils.DataSocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpCachedPoolServer extends TcpBlockingProcessor {
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public TcpCachedPoolServer(int port) {
        super(port);
    }

    @Override
    protected void processClient(DataSocket client) {
        pool.execute(getAllQueryExecutor(client));
    }
}
