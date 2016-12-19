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

public final class Server implements Runnable {
    private final ServerFactory serverFactory = new ServerFactory();

    private final int port;

    private IServer sortServer;

    private volatile Socket client;
    private volatile ServerSocket serverSocket;
    private Thread currentThread = null;

    public Server(final int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                this.serverSocket = serverSocket;
                client = serverSocket.accept();
                processClient(client);
            }
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
                        if (currentThread != null) {
                            sortServer.stop();
                            currentThread.join();
                        }

                        sortServer = serverFactory.create(params);
                        currentThread = new Thread(sortServer);
                        currentThread.start();
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
            if (currentThread != null) {
                currentThread.interrupt();
                try {
                    currentThread.join();
                } catch (InterruptedException e1) {
                    System.out.printf("Double stop server: %s%n", e1.getMessage());
                }
            }
        } catch (InterruptedException e) {
            // server stopped
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
