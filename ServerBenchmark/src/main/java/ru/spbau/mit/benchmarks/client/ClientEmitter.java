package ru.spbau.mit.benchmarks.client;

import ru.spbau.mit.benchmarks.utils.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public final class ClientEmitter implements Runnable {
    private final List<Pair<Client, Thread>> clients = new LinkedList<>();
    private AtomicLong totalWorkTime = new AtomicLong();

    public ClientEmitter(int clientsCount, Function<Integer, Client> clientFactory, boolean udp, int clientPort) {
        for (int i = 0; i < clientsCount; ++i) {
            final Client client = clientFactory.apply(udp ? clientPort + i : clientPort);
            clients.add(new Pair<>(client, new Thread(client::task)));
        }
    }

    @Override
    public void run() {
        for (final Pair<Client, Thread> client : clients) {
            client.second.start();
        }

        try {
            long totalWorkTime = 0;
            for (final Pair<Client, Thread> client : clients) {
                client.second.join();
                totalWorkTime += client.first.getWorkTime();
            }
            this.totalWorkTime.set(totalWorkTime);
        } catch (InterruptedException e) {
            System.out.printf("Benchmark interrupted: %s%n", e.getMessage());
        }
    }

    public long getTotalWorkTime() {
        return totalWorkTime.get();
    }
}
