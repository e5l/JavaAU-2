package ru.spbau.mit.benchmarks.utils;

public final class Metrics {
    private final double averageClientWorkTime;
    private final double averageRequestTime;
    private final double averageSortTime;

    public Metrics(double averageClientWorkTime, double averageRequestTime, double averageSortTime) {
        this.averageClientWorkTime = averageClientWorkTime;
        this.averageRequestTime = averageRequestTime;
        this.averageSortTime = averageSortTime;
    }

    public double getAverageSortTime() {
        return averageSortTime;
    }

    public double getAverageRequestTime() {
        return averageRequestTime;
    }

    public double getAverageClientWorkTime() {
        return averageClientWorkTime;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", averageClientWorkTime, averageRequestTime, averageSortTime);
    }
}
