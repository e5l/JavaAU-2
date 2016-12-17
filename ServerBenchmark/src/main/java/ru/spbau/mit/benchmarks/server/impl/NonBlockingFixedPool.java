package ru.spbau.mit.benchmarks.server.impl;

import ru.spbau.mit.benchmarks.generated.MetricsResponseOuterClass;
import ru.spbau.mit.benchmarks.server.IServer;

public class NonBlockingFixedPool implements IServer {
    @Override
    public void run() {
    }
    @Override
    public int getPort() {
        return 0;
    }
    @Override
    public MetricsResponseOuterClass.MetricsResponse getMetrics() {
        return null;
    }
    @Override
    public void stop() {
    }

}
