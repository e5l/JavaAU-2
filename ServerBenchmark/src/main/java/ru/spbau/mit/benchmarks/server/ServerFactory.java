package ru.spbau.mit.benchmarks.server;

import ru.spbau.mit.benchmarks.generated.BenchmarkParamsOuterClass;
import ru.spbau.mit.benchmarks.server.impl.TcpThreadServer;

public final class ServerFactory {
    final int PORT = 9090;

    public IServer create(BenchmarkParamsOuterClass.BenchmarkParams.ServerType type) {
        switch (type) {
            case TCP_THREAD_CLIENT:
                return new TcpThreadServer(PORT);
            case TCP_CACHED_POOL:
                break;
            case TCP_FIXED_POOL:
                break;
            case TCP_CONNECTION_QUERY:
                break;
            case TCP_ASYNC:
                break;
            case UDP_THREAD:
                break;
            case UDP_POOL:
                break;
            default:
                break;
        }

        return null;
    }
}
