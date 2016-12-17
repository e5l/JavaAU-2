package ru.spbau.mit.benchmarks.server;

import ru.spbau.mit.benchmarks.generated.MetricsResponseOuterClass;

public interface IServer extends Runnable {

    @Override
    void run();

    int getPort();
    MetricsResponseOuterClass.MetricsResponse getMetrics();

    void stop();
}
