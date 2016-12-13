package ru.spbau.mit.benchmarks.client;

import ru.spbau.mit.benchmarks.generated.SortDataOuterClass;
import ru.spbau.mit.benchmarks.utils.DataSocket;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public final class TCPSortingClient extends Client {
    private final String host;
    private final int port;
    private final int messageDelay;
    private final boolean singleConnection;

    public TCPSortingClient(String host, int port, int arraySize, int requestsCount, int messageDelay, boolean singleConnection) {
        super(arraySize, requestsCount);
        this.host = host;
        this.port = port;
        this.messageDelay = messageDelay;
        this.singleConnection = singleConnection;
    }

    @Override
    public void task() {
        try {
            Socket socket = new Socket(host, port);

            for (List<Integer> item : data) {
                sortArray(item, socket);
                Thread.sleep(messageDelay);

                if (!singleConnection) {
                    socket.close();
                    socket = new Socket(host, port);
                }
            }
        } catch (IOException e) {
            System.out.printf("Failed to process task: %s%n", e.getMessage());
        } catch (InterruptedException e) {
            System.out.printf("Benchmark interrupted: %s%n", e.getMessage());
        }
    }

    private void sortArray(final List<Integer> item, final Socket socket) throws IOException {
        final DataSocket data = new DataSocket(socket);
        data.write(SortDataOuterClass.SortData.newBuilder().addAllData(item).build());
        byte[] response = data.read();
    }
}
