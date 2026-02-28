package me.molybdenum.ambience_mini.engine.shared.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Maybe<T>
{
    @Nullable
    private final T value;
    private final boolean hasValue;


    protected Maybe(@Nullable T value) {
        this.value = value;
        this.hasValue = value != null;
    }


    public boolean hasValue() {
        return hasValue;
    }

    public T getValue() {
        if (!hasValue)
            throw new RuntimeException("Cannot get value of maybe without a value.");
        return value;
    }


    public static <T> Maybe<T> of(@NotNull T value) {
        return new Maybe<>(Objects.requireNonNull(value));
    }

    public static <T> Maybe<T> none() {
        return new Maybe<>(null);
    }
}
