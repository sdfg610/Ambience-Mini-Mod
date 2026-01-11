package me.molybdenum.ambience_mini.engine.utils;

@FunctionalInterface
public interface QuadConsumer<T, U, V, N> {
    void apply(T t, U u, V v, N n);
}
