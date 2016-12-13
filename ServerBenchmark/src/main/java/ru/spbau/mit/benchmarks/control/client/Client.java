package ru.spbau.mit.benchmarks.control.client;

import ru.spbau.mit.benchmarks.client.ClientEmitter;
import ru.spbau.mit.benchmarks.client.TCPSortingClient;
import ru.spbau.mit.benchmarks.generated.BenchmarkParamsOuterClass;
import ru.spbau.mit.benchmarks.generated.BenchmarkResult;
import ru.spbau.mit.benchmarks.generated.ControlOuterClass;
import ru.spbau.mit.benchmarks.generated.InitResponseOuterClass;
import ru.spbau.mit.benchmarks.utils.DataSocket;

import java.io.IOException;
import java.net.Socket;

public final class Client {
    private final String host;
    private final int port;

    public Client(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    public BenchmarkResult measure(final BenchmarkParamsOuterClass.BenchmarkParams params) throws IOException {
        InitResponseOuterClass.InitResponse initResponse = initTestServer(params);
        return runBenchmarks(params, initResponse.getPort());
    }

    private BenchmarkResult runBenchmarks(BenchmarkParamsOuterClass.BenchmarkParams params, int port) {
        boolean udp = (params.getType() == BenchmarkParamsOuterClass.BenchmarkParams.ServerType.UDP_POOL
                || params.getType() == BenchmarkParamsOuterClass.BenchmarkParams.ServerType.UDP_THREAD);

        final ClientEmitter emitter = new ClientEmitter(params.getClientsCount(), udp ?
                () -> null :
                () -> new TCPSortingClient(host,
                        port,
                        params.getArraySize(),
                        params.getRequestsCount(),
                        params.getMessageDelay(),
                        params.getType() == BenchmarkParamsOuterClass.BenchmarkParams.ServerType.TCP_CONNECTION_QUERY));

        emitter.run();

        return null;
    }

    private InitResponseOuterClass.InitResponse initTestServer(final BenchmarkParamsOuterClass.BenchmarkParams params) throws IOException {
        final Socket connection = new Socket(host, port);
        final DataSocket socket = new DataSocket(connection);

        socket.write(ControlOuterClass.Control.newBuilder().setType(ControlOuterClass.Control.Type.INIT_SERVER).build());
        socket.write(params);

        return InitResponseOuterClass.InitResponse.parseFrom(socket.read());
    }
}
