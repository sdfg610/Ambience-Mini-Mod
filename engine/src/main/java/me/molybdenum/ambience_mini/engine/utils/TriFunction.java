package me.molybdenum.ambience_mini.engine.utils;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    R apply(T t, U u, V v);
}
