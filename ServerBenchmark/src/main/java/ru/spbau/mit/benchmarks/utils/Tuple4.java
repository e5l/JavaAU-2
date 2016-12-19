package ru.spbau.mit.benchmarks.utils;

public class Tuple4<T1, T2, T3, T4> {
    public final T1 t1;
    public final T2 t2;
    public final T3 t3;
    public final T4 t4;

    public Tuple4(final T1 t1, final T2 t2, final T3 t3, final T4 t4) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.t4 = t4;
    }
}
