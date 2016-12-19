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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class UdpThreadServer implements IServer {
    private final int port;
    private final int clientsCount;
    private final boolean pool;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private AtomicLong totalSortTime = new AtomicLong();
    private AtomicLong totalRequestTime = new AtomicLong();
    private volatile boolean closed = false;
    private ConcurrentLinkedQueue<DatagramSocket> sockets = new ConcurrentLinkedQueue<>();

    public UdpThreadServer(final int port, final int clientsCount, boolean pool) {
        this.port = port;
        this.clientsCount = clientsCount;
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

        public Handler(final int currentPort, int requestIndex) {
            this.currentPort = currentPort;
        }

        @Override
        public void run() {
            try (final DatagramSocket socket = new DatagramSocket(currentPort)) {
                sockets.add(socket);
                while (!closed) {
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
                }
            } catch (IOException e) {
                // socket closed
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
        closed = true;
        for (DatagramSocket socket : sockets) {
            socket.close();
        }
    }
}
