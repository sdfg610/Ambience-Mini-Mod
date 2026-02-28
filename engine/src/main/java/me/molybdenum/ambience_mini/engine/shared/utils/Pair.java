package me.molybdenum.ambience_mini.engine.shared.utils;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public record Pair<T1, T2>(T1 left, T2 right) {
    public void deconstructVoid(BiConsumer<T1, T2> body) {
        body.accept(left, right);
    }

    public <E> E destruct(BiFunction<T1, T2, E> body) {
        return body.apply(left, right);
    }
}
