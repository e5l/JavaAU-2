package ru.spbau.mit.benchmarks.server.tcp;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.benchmarks.generated.MetricsResponseOuterClass;
import ru.spbau.mit.benchmarks.generated.SortDataOuterClass;
import ru.spbau.mit.benchmarks.server.IServer;
import ru.spbau.mit.benchmarks.server.InsertionSort;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TcpAsync implements IServer {
    private final int port;
    private final AsynchronousChannelGroup group = AsynchronousChannelGroup
            .withFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());

    private AtomicLong totalRequestTime = new AtomicLong(0);
    private AtomicLong totalSortTime = new AtomicLong(0);
    private AsynchronousServerSocketChannel server;

    public TcpAsync(final int port) throws IOException {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            server = AsynchronousServerSocketChannel.open(group).bind(new InetSocketAddress(port));
            while (server.isOpen()) {
                final AsynchronousSocketChannel channel = server.accept().get();
                final ByteBuffer size = ByteBuffer.allocate(4);
                channel.read(size, channel, new SizeReader(size));
            }
        } catch (IOException e) {
            System.out.printf("Failed to start server: %s%n", e.getMessage());
        } catch (InterruptedException | ExecutionException e) {
            // server stopped
        }
    }

    @Override
    public int getPort() {
        return port;
    }
    @Override
    public MetricsResponseOuterClass.MetricsResponse getMetrics() {
        return MetricsResponseOuterClass.MetricsResponse.newBuilder()
                .setTotalRequestTime(totalRequestTime.get()).setTotalSortTime(totalSortTime.get()).build();
    }
    @Override
    public void stop() {
        try {
            server.close();
        } catch (IOException e) {
            // already closed
        }
    }

    private class SizeReader implements CompletionHandler<Integer, AsynchronousSocketChannel> {
        private final ByteBuffer size;

        public SizeReader(ByteBuffer size) {
            this.size = size;
        }

        @Override
        public void completed(Integer result, AsynchronousSocketChannel data) {
            if (size.remaining() > 0) {
                data.read(size, data, this);
                return;
            }

            long startTime = System.currentTimeMillis();

            size.flip();
            final int packetSize = size.getInt();
            final ByteBuffer packet = ByteBuffer.allocate(packetSize);
            data.read(packet, data, new ProtoReader(packet, startTime));
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {

        }
    }

    private class ProtoReader implements CompletionHandler<Integer, AsynchronousSocketChannel> {
        private final ByteBuffer packet;
        private final long startTime;

        public ProtoReader(ByteBuffer packet, long startTime) {
            this.packet = packet;
            this.startTime = startTime;
        }

        @Override
        public void completed(Integer result, AsynchronousSocketChannel channel) {
            if (packet.remaining() > 0) {
                channel.read(packet, channel, this);
                return;
            }

            try {
                final int[] sortData = SortDataOuterClass.SortData.parseFrom(packet.array()).getDataList().stream().mapToInt(i -> i).toArray();

                long sortStart = System.currentTimeMillis();
                InsertionSort.sort(sortData);
                totalSortTime.getAndAdd(System.currentTimeMillis() - sortStart);

                final ByteBuffer packet = ByteBuffer.wrap(SortDataOuterClass.SortData.newBuilder()
                        .addAllData(Arrays.stream(sortData).mapToObj(i -> i).collect(Collectors.toList()))
                        .build().toByteArray());

                final ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
                sizeBuffer.putInt(packet.limit());
                sizeBuffer.flip();
                while (sizeBuffer.remaining() > 0) {
                    channel.write(sizeBuffer).get();
                }

                channel.write(packet, channel, new PacketWriter(packet, startTime));
            } catch (InvalidProtocolBufferException e) {
                System.out.printf("Invalid protobuf: %s%n", e.getMessage());
            } catch (InterruptedException | ExecutionException e) {
                // server early stop
            }
        }
        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {

        }
    }

    private class PacketWriter implements CompletionHandler<Integer, AsynchronousSocketChannel> {
        private final ByteBuffer packet;
        private final long startTime;

        public PacketWriter(ByteBuffer packet, long startTime) {
            this.packet = packet;
            this.startTime = startTime;
        }

        @Override
        public void completed(Integer result, AsynchronousSocketChannel channel) {
            if (packet.remaining() > 0) {
                channel.write(packet, null, this);
                return;
            }

            totalRequestTime.getAndAdd(System.currentTimeMillis() - startTime);
            try {
                channel.close();
            } catch (IOException e) {
                // already closed
            }
        }

        @Override
        public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
        }
    }
}
