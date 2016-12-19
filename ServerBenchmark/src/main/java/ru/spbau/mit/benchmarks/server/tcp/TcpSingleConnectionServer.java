package ru.spbau.mit.benchmarks.server.tcp;

import ru.spbau.mit.benchmarks.utils.DataSocket;
import ru.spbau.mit.benchmarks.utils.Pair;

import java.io.IOException;

public class TcpSingleConnectionServer extends TcpBlockingProcessor {

    public TcpSingleConnectionServer(int port) {
        super(port);
    }

    @Override
    protected void processClient(DataSocket client) {
        try {
            Pair<Long, Long> metrics = processRequest(client);
            totalRequestTime.addAndGet(metrics.first);
            totalSortTime.addAndGet(metrics.second);
            client.close();
        } catch (IOException e) {
            // already closed
        }
    }
}
