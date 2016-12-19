package ru.spbau.mit.benchmarks.server.udp;

import ru.spbau.mit.benchmarks.generated.MetricsResponseOuterClass;
import ru.spbau.mit.benchmarks.generated.SortDataOuterClass;
import ru.spbau.mit.benchmarks.server.IServer;
import ru.spbau.mit.benchmarks.server.InsertionSort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class UdpThreadServer implements IServer {
    private final int port;
    private final int clientsCount;
    private final int requestsCount;
    private final boolean pool;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private AtomicLong totalSortTime = new AtomicLong();
    private AtomicLong totalRequestTime = new AtomicLong();

    public UdpThreadServer(final int port, final int clientsCount, final int requestsCount, boolean pool) {
        this.port = port;
        this.clientsCount = clientsCount;
        this.requestsCount = requestsCount;
        this.pool = pool;
    }

    @Override
    public void run() {
        for (int i = port; i < port + clientsCount; ++i) {
            startHandler(new Handler(i, 0));
        }
    }

    private void startHandler(Handler handler) {
        if (!pool) {
            new Thread(handler).start();
        } else {
            threadPool.execute(handler);
        }
    }

    private class Handler implements Runnable {
        private final int currentPort;
        private final int requestIndex;

        public Handler(final int currentPort, int requestIndex) {
            this.currentPort = currentPort;
            this.requestIndex = requestIndex;
        }

        @Override
        public void run() {
            try {
                final DatagramSocket socket = new DatagramSocket(currentPort);
                final byte[] buffer = new byte[65507];
                final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                final long startTime = System.currentTimeMillis();
                final ByteBuffer data = ByteBuffer.wrap(buffer);
                final int size = data.getInt();
                final byte[] proto = new byte[size];
                data.get(proto);

                final int[] sortData = SortDataOuterClass.SortData
                        .parseFrom(proto).getDataList().stream().mapToInt(i -> i).toArray();

                final long sortStart = System.currentTimeMillis();
                InsertionSort.sort(sortData);
                totalSortTime.getAndAdd(System.currentTimeMillis() - sortStart);

                final SortDataOuterClass.SortData build = SortDataOuterClass.SortData.newBuilder()
                        .addAllData(Arrays.stream(sortData).boxed().collect(Collectors.toList())).build();

                final ByteBuffer result = ByteBuffer.allocate(4 + build.getSerializedSize());
                result.putInt(build.getSerializedSize());
                result.put(build.toByteArray());

                socket.send(new DatagramPacket(result.array(),
                        result.array().length,
                        packet.getAddress(),
                        packet.getPort()));

                totalRequestTime.getAndAdd(System.currentTimeMillis() - startTime);
                socket.close();
                if (requestIndex <= requestsCount) {
                    startHandler(new Handler(currentPort, requestIndex + 1));
                }
            } catch (SocketException e) {
                // socket closed
            } catch (IOException e) {
                System.out.printf("Failed to receive packet: %s%n", e.getMessage());
            }
        }
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public MetricsResponseOuterClass.MetricsResponse getMetrics() {
        return MetricsResponseOuterClass.MetricsResponse.newBuilder()
                .setTotalSortTime(totalSortTime.get()).setTotalRequestTime(totalRequestTime.get()).build();
    }
    @Override
    public void stop() {

    }
}
