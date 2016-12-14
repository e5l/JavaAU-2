package ru.spbau.mit.benchmarks.control.server;

import ru.spbau.mit.benchmarks.exceptions.ServerUninitializedException;
import ru.spbau.mit.benchmarks.generated.BenchmarkParamsOuterClass;
import ru.spbau.mit.benchmarks.generated.ControlOuterClass;
import ru.spbau.mit.benchmarks.generated.InitResponseOuterClass;
import ru.spbau.mit.benchmarks.generated.MetricsResponseOuterClass;
import ru.spbau.mit.benchmarks.server.IServer;
import ru.spbau.mit.benchmarks.server.ServerFactory;
import ru.spbau.mit.benchmarks.utils.DataSocket;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    private final ServerFactory serverFactory = new ServerFactory();

    private final int port;

    private IServer sortServer;

    private volatile Socket client;
    private volatile ServerSocket serverSocket;

    public Server(final int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            this.serverSocket = serverSocket;
            client = serverSocket.accept();
            processClient(client);
        } catch (InterruptedIOException e) {
            System.out.println("Server interrupted.");
        } catch (IOException e) {
            // socket closed
        }
    }

    private void processClient(final Socket socket) {
        try (final DataSocket client = new DataSocket(socket)) {
            while (true) {
                final ControlOuterClass.Control.Type type = ControlOuterClass.Control.parseFrom(client.read()).getType();
                switch (type) {
                    case INIT_SERVER:
                        final BenchmarkParamsOuterClass.BenchmarkParams params = BenchmarkParamsOuterClass.BenchmarkParams.parseFrom(client.read());
                        sortServer = serverFactory.create(params.getType());
                        new Thread(sortServer).start();
                        client.write(InitResponseOuterClass.InitResponse.newBuilder().setPort(sortServer.getPort()).build());
                        break;
                    case REQUEST_METRICS:
                        client.write(requestMetrics());
                        break;
                    default:
                        System.out.printf("Unknown message type: %s%n", type.toString());
                        break;
                }
            }
        } catch (IOException e) {
            sortServer.stop();
        }
    }

    private MetricsResponseOuterClass.MetricsResponse requestMetrics() {
        if (sortServer == null) {
            throw new ServerUninitializedException();
        }

        return sortServer.getMetrics();
    }

    public void stop() throws IOException {
        if (client != null) {
            client.close();
        }

        if (serverSocket != null) {
            serverSocket.close();
        }
    }
}
