package ru.spbau.mit.benchmarks.server;

import ru.spbau.mit.benchmarks.generated.BenchmarkParamsOuterClass;
import ru.spbau.mit.benchmarks.server.tcp.*;
import ru.spbau.mit.benchmarks.server.udp.UdpThreadServer;

import java.io.IOException;

public final class ServerFactory {
    final int PORT = 9090;

    public IServer create(BenchmarkParamsOuterClass.BenchmarkParams type) throws IOException {
        switch (type.getType()) {
            case TCP_THREAD_CLIENT:
                return new TcpThreadServer(PORT);
            case TCP_CACHED_POOL:
                return new TcpCachedPoolServer(PORT);
            case TCP_FIXED_POOL:
                return new TcpNonBlockingFixedPool(PORT);
            case TCP_CONNECTION_QUERY:
                return new TcpSingleConnectionServer(PORT);
            case TCP_ASYNC:
                return new TcpAsync(PORT);
            case UDP_THREAD:
                return new UdpThreadServer(PORT, type.getClientsCount(), false);
            case UDP_POOL:
                return new UdpThreadServer(PORT, type.getClientsCount(), true);
            default:
                break;
        }

        return null;
    }
}
