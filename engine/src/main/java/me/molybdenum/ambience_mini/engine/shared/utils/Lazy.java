package me.molybdenum.ambience_mini.engine.shared.utils;

import java.util.function.Supplier;

public class Lazy<T> {
    private T value;
    private boolean notInitialized = true;
    private final Supplier<T> getter;


    public Lazy(Supplier<T> getter) {
        this.getter = getter;
    }


    public T get() {
        if (notInitialized) {
            value = getter.get();
            notInitialized = false;
        }
        return value;
    }
}
