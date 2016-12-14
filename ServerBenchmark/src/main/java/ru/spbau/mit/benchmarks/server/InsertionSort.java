package ru.spbau.mit.benchmarks.server;

public final class InsertionSort {

    public static void sort(int data[]) {
        int length = data.length;
        for (int i = 0; i < length; i++) {
            int j = i;
            while (j > 0 && data[j - 1] > data[j]) {
                int tmp = data[j - 1];
                data[j - 1] = data[j];
                data[j] = tmp;
                j--;
            }
        }
    }
}
