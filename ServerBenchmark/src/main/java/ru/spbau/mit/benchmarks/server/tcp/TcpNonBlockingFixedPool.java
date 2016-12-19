package ru.spbau.mit.benchmarks.server.tcp;

import ru.spbau.mit.benchmarks.generated.MetricsResponseOuterClass;
import ru.spbau.mit.benchmarks.generated.SortDataOuterClass;
import ru.spbau.mit.benchmarks.server.IServer;
import ru.spbau.mit.benchmarks.server.InsertionSort;
import ru.spbau.mit.benchmarks.utils.Pair;
import ru.spbau.mit.benchmarks.utils.Tuple4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class TcpNonBlockingFixedPool implements IServer {
    private static final String PUBLIC_HOST = "0.0.0.0";

    private final int port;
    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private final Queue<Future<Tuple4<Long, Long, byte[], SocketChannel>>> resultsQueue = new LinkedList<>();
    private final Map<SocketChannel, Queue<Pair<Long, byte[]>>> results = new HashMap<>();

    private int totalRequestTime = 0;
    private int totalSortTime = 0;
    private ServerSocketChannel channel;

    public TcpNonBlockingFixedPool(final int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            final Selector selector = SelectorProvider.provider().openSelector();
            channel = ServerSocketChannel.open();

            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(PUBLIC_HOST, port));
            channel.register(selector, SelectionKey.OP_ACCEPT);

            while (channel.isOpen()) {
                selector.selectNow();
                final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    final SelectionKey next = iterator.next();
                    iterator.remove();

                    if (!next.isValid()) {
                        results.remove(next.channel());
                        continue;
                    }

                    if (next.isAcceptable()) {
                        final ServerSocketChannel clientChannel = (ServerSocketChannel) next.channel();
                        SocketChannel client = clientChannel.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                    } else if (next.isReadable()) {
                        final SocketChannel client = (SocketChannel) next.channel();
                        processRequest(client);
                        next.interestOps(0);
                    } else if (next.isWritable()) {
                        write(next);
                        next.interestOps(SelectionKey.OP_READ);
                    }
                }

                while (resultsQueue.peek() != null && resultsQueue.peek().isDone()) {
                    Tuple4<Long, Long, byte[], SocketChannel> result = resultsQueue.peek().get();

                    if (!results.containsKey(result.t4)) {
                        results.put(result.t4, new LinkedList<>());
                    }

                    final SelectionKey key = result.t4.keyFor(selector);
                    key.attach(new Pair<>(result.t1, result.t3));
                    key.interestOps(SelectionKey.OP_WRITE);

                    resultsQueue.poll();
                    totalSortTime += result.t2;
                }
            }

        } catch (IOException e) {
            System.out.printf("Failed to run server: %s%n", e.getMessage());
        } catch (InterruptedException e) {
            // server stopped
        } catch (ExecutionException e) {
            System.out.printf("Failed to sort array: %s%n", e.getMessage());
        }
    }

    private void write(final SelectionKey next) throws IOException {
        if (next.attachment() == null) {
            throw new RuntimeException("Wrong tasks queue state");
        }

        final Pair<Long, byte[]> item = (Pair<Long, byte[]>) next.attachment();
        final ByteBuffer buffer = ByteBuffer.wrap(item.second);
        final ByteBuffer size = ByteBuffer.allocate(4);
        final SocketChannel channel = (SocketChannel) next.channel();
        size.putInt(buffer.remaining());
        size.flip();

        int i = 0;
        while (size.remaining() > 0) {
            i += channel.write(size);
        }

        while (buffer.remaining() > 0) {
            i += channel.write(buffer);
        }

        totalRequestTime += System.currentTimeMillis() - item.first;
    }

    private void processRequest(final SocketChannel client) throws IOException {
        final long startTime = System.currentTimeMillis();

        ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
        while (sizeBuffer.remaining() > 0) {
            int result = client.read(sizeBuffer);
            if (result < 0) {
                return;
            }
        }

        sizeBuffer.flip();
        final int size = sizeBuffer.getInt();

        ByteBuffer message = ByteBuffer.allocate(size);
        while (message.remaining() > 0) {
            client.read(message);
        }

        message.flip();
        resultsQueue.add(pool.submit(new PoolTask(message.array(), startTime, client)));
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public MetricsResponseOuterClass.MetricsResponse getMetrics() {
        return MetricsResponseOuterClass.MetricsResponse
                .newBuilder()
                .setTotalRequestTime(totalRequestTime)
                .setTotalSortTime(totalSortTime)
                .build();
    }

    @Override
    public void stop() {
        try {
            channel.close();
        } catch (IOException e) {
            // already closed
        }
    }

    private class PoolTask implements Callable<Tuple4<Long, Long, byte[], SocketChannel>> {
        private final byte[] data;
        private final long startTime;
        private final SocketChannel client;

        public PoolTask(byte[] data, long startTime, SocketChannel client) {
            this.data = data;
            this.startTime = startTime;
            this.client = client;
        }

        @Override
        public Tuple4<Long, Long, byte[], SocketChannel> call() throws Exception {
            int[] sortData = SortDataOuterClass.SortData
                    .parseFrom(data).getDataList().stream().mapToInt(i -> i).toArray();

            long sortStart = System.currentTimeMillis();
            InsertionSort.sort(sortData);
            final long sortTime = System.currentTimeMillis() - sortStart;
            return new Tuple4<>(
                    startTime,
                    sortTime,
                    SortDataOuterClass.SortData.newBuilder()
                            .addAllData(Arrays.stream(sortData).mapToObj(i -> i).collect(Collectors.toList()))
                            .build().toByteArray(), client);
        }
    }
}
