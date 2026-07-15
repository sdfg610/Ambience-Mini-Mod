package me.molybdenum.ambience_mini.engine.shared.utils;

public class Deferred<T> {
    private boolean isSet = false;
    private T value;

    public T get() {
        if (isSet)
            return value;
        throw new RuntimeException("Deferred value is not yet set!");
    }

    public synchronized T set(T value) {
        if (isSet)
            throw new RuntimeException("Deferred value has already been set!");
        this.value = value;
        this.isSet = true;
        return value;
    }
}
