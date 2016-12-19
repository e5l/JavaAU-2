package ru.spbau.mit.benchmarks.client;

import ru.spbau.mit.benchmarks.generated.SortDataOuterClass;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class UDPSortingClient extends Client {
    private final int delay;
    private final int port;
    private final String host;
    private final AtomicLong workTime = new AtomicLong();

    public UDPSortingClient(final int arraySize, final int requestsCount, final int delay, final int port, final String host) {
        super(arraySize, requestsCount);
        this.delay = delay;
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
                socket.close();
                Thread.sleep(delay);
            } catch (IOException | InterruptedException e) {
                // failed to create socket
            }
        }
        workTime.getAndAdd(System.currentTimeMillis() - startTime);
    }
    private void sortArray(List<Integer> item, DatagramSocket socket) throws IOException {
        socket.setSoTimeout(item.size() * 2);

        final SortDataOuterClass.SortData build = SortDataOuterClass.SortData.newBuilder().addAllData(item).build();
        byte[] packet = new byte[4 + build.getSerializedSize()];
        byte[] buff = new byte[65507];
        final ByteBuffer buffer = ByteBuffer.wrap(packet);

        buffer.putInt(build.getSerializedSize());
        buffer.put(build.toByteArray());

        final DatagramPacket message = new DatagramPacket(packet, packet.length, InetAddress.getByName(host), port);
        while (true) {
            try {
                socket.send(message);
                socket.receive(new DatagramPacket(buff, buff.length));
                break;
            } catch (SocketTimeoutException e) {
                // timeout
            }
        }
    }

    @Override
    public long getWorkTime() {
        return workTime.get();
    }
}
