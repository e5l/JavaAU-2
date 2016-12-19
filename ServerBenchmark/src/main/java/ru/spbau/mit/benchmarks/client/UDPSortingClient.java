package ru.spbau.mit.benchmarks.client;

import ru.spbau.mit.benchmarks.generated.SortDataOuterClass;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class UDPSortingClient extends Client {
    private final int port;
    private final String host;
    private final AtomicLong workTime = new AtomicLong();

    public UDPSortingClient(int arraySize, int requestsCount, int port, String host) {
        super(arraySize, requestsCount);
        this.port = port;
        this.host = host;
    }

    @Override
    public void task() {
        final long startTime = System.currentTimeMillis();
        for (final List<Integer> item : data) {
            try {
                final DatagramSocket socket = new DatagramSocket(port);
                sortArray(item, socket);
            } catch (IOException e) {
                // failed to create socket
            }
        }
        workTime.getAndAdd(System.currentTimeMillis() - startTime);
    }
    private void sortArray(List<Integer> item, DatagramSocket socket) throws IOException {
        final SortDataOuterClass.SortData build = SortDataOuterClass.SortData.newBuilder().addAllData(item).build();
        byte[] packet = new byte[4 + build.getSerializedSize()];
        final ByteBuffer buffer = ByteBuffer.wrap(packet);

        buffer.putInt(build.getSerializedSize());
        buffer.put(build.toByteArray());

        final DatagramPacket message = new DatagramPacket(packet, packet.length, InetAddress.getByName(host), port);
        socket.send(message);

        byte[] buff = new byte[4098];
        socket.receive(new DatagramPacket(buff, buff.length));
    }

    @Override
    public long getWorkTime() {
        return workTime.get();
    }
}
