package ru.spbau.mit.benchmarks.server.impl;

import ru.spbau.mit.benchmarks.generated.MetricsResponseOuterClass;
import ru.spbau.mit.benchmarks.generated.SortDataOuterClass;
import ru.spbau.mit.benchmarks.server.IServer;
import ru.spbau.mit.benchmarks.server.InsertionSort;
import ru.spbau.mit.benchmarks.utils.DataSocket;
import ru.spbau.mit.benchmarks.utils.Pair;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TcpThreadServer implements IServer {
    private final int port;
    private ServerSocket server;
    private final AtomicLong totalRequestTime = new AtomicLong();
    private final AtomicLong totalSortTime = new AtomicLong();

    public TcpThreadServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (final ServerSocket server = new ServerSocket(port)) {
            this.server = server;
            while (!Thread.interrupted()) {
                final DataSocket client = new DataSocket(server.accept());
                new Thread(() -> processClient(client)).start();
            }
        } catch (IOException e) {
            // socket closed
        }
    }

    private void processClient(DataSocket client) {
        long requestTime = 0;
        long sortTime = 0;
        try {
            while (true) {
                Pair<Long, byte[]> message = client.readAndMeasure();
                int[] data = SortDataOuterClass.SortData.parseFrom(message.second)
                        .getDataList().stream().mapToInt(x -> x).toArray();

                long sortStart = System.currentTimeMillis();
                InsertionSort.sort(data);
                sortTime += System.currentTimeMillis() - sortStart;

                client.write(SortDataOuterClass.SortData.newBuilder()
                        .addAllData(Arrays.stream(data).boxed().collect(Collectors.toList())).build());

                requestTime += System.currentTimeMillis() - message.first;
            }
        } catch (IOException e) {
            // socket closed
        }

        totalRequestTime.addAndGet(requestTime);
        totalSortTime.addAndGet(sortTime);
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public MetricsResponseOuterClass.MetricsResponse getMetrics() {
        return MetricsResponseOuterClass.MetricsResponse.newBuilder()
                .setTotalRequestTime(totalRequestTime.get())
                .setTotalSortTime(totalSortTime.get())
                .build();
    }

    @Override
    public void stop() {
        try {
            server.close();
        } catch (IOException e) {
            System.out.printf("Failed to stop %s: %s%n", TcpThreadServer.class.getName(), e.getMessage());
        }
    }
}
