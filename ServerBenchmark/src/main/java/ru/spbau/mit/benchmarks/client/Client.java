package ru.spbau.mit.benchmarks.client;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class Client {
    private static final int MAX_N = Integer.MAX_VALUE;

    protected final List<List<Integer>> data = new LinkedList<>();

    protected Client(final int arraySize, final int requestsCount) {
        for (int i = 0; i < requestsCount; ++i) {
            data.add(generate(arraySize));
        }
    }

    abstract void task();

    private List<Integer> generate(int size) {
        final Random random = new Random();
        final List<Integer> result = new LinkedList<>();
        for (int i = 0; i < size; ++i) {
            result.add(random.nextInt(MAX_N));
        }

        return result;
    }
}
