package ru.spbau.mit.benchmarks.control.client;

import ru.spbau.mit.benchmarks.client.ClientEmitter;
import ru.spbau.mit.benchmarks.client.TCPSortingClient;
import ru.spbau.mit.benchmarks.client.UDPSortingClient;
import ru.spbau.mit.benchmarks.generated.BenchmarkParamsOuterClass;
import ru.spbau.mit.benchmarks.generated.ControlOuterClass;
import ru.spbau.mit.benchmarks.generated.InitResponseOuterClass;
import ru.spbau.mit.benchmarks.generated.MetricsResponseOuterClass;
import ru.spbau.mit.benchmarks.utils.DataSocket;
import ru.spbau.mit.benchmarks.utils.Metrics;

import java.io.IOException;
import java.net.Socket;

public final class Client {
    private final String host;
    private final int port;

    public Client(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public Metrics measure(final BenchmarkParamsOuterClass.BenchmarkParams params) throws IOException {
        try (final DataSocket socket = new DataSocket(new Socket(host, port))) {
            int sortPort = initTestServer(params, socket).getPort();

            boolean udp = (params.getType() == BenchmarkParamsOuterClass.BenchmarkParams.ServerType.UDP_POOL
                    || params.getType() == BenchmarkParamsOuterClass.BenchmarkParams.ServerType.UDP_THREAD);

            final ClientEmitter emitter = new ClientEmitter(params.getClientsCount(), udp ?
                    (Integer port) -> new UDPSortingClient(
                            params.getArraySize(),
                            params.getRequestsCount(),
                            sortPort,
                            host) :
                    (Integer port) -> new TCPSortingClient(host,
                            port,
                            params.getArraySize(),
                            params.getRequestsCount(),
                            params.getMessageDelay(),
                            params.getType() != BenchmarkParamsOuterClass.BenchmarkParams.ServerType.TCP_CONNECTION_QUERY ||
                                    params.getType() != BenchmarkParamsOuterClass.BenchmarkParams.ServerType.TCP_ASYNC),
                    udp, sortPort);

            emitter.run();
            final long totalClientsWorkTime = emitter.getTotalWorkTime();
            final MetricsResponseOuterClass.MetricsResponse metricsResponse = receiveMetrics(socket);
            final double requestsCount = params.getClientsCount() * params.getRequestsCount();
            return new Metrics(
                    1.0 * totalClientsWorkTime / params.getClientsCount(),
                    metricsResponse.getTotalRequestTime() / requestsCount,
                    metricsResponse.getTotalSortTime() / requestsCount);
        }
    }

    private InitResponseOuterClass.InitResponse initTestServer(final BenchmarkParamsOuterClass.BenchmarkParams params, final DataSocket socket) throws IOException {
        socket.write(ControlOuterClass.Control.newBuilder().setType(ControlOuterClass.Control.Type.INIT_SERVER).build());
        socket.write(params);
        return InitResponseOuterClass.InitResponse.parseFrom(socket.read());
    }

    public MetricsResponseOuterClass.MetricsResponse receiveMetrics(final DataSocket socket) throws IOException {
        socket.write(ControlOuterClass.Control.newBuilder().setType(ControlOuterClass.Control.Type.REQUEST_METRICS).build());
        return MetricsResponseOuterClass.MetricsResponse.parseFrom(socket.read());
    }
}
