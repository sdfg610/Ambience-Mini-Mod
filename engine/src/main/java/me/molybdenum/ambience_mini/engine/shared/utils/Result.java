package me.molybdenum.ambience_mini.engine.shared.utils;

import java.util.function.Function;


public class Result<T> {
    private final boolean isSuccess;

    public final T value;
    public final String error;


    private Result(boolean isSuccess, T value, String error) {
        this.isSuccess = isSuccess;
        this.value = value;
        this.error = error;
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isSuccess() {
        return isSuccess;
    }

    public T getValue() {
        if (isSuccess)
            return value;
        throw new IllegalStateException("Tried to get value of failure-result.");
    }

    public <E> Result<E> map(Function<T, E> onResult) {
        return isSuccess
                ? Result.of(onResult.apply(value))
                : Result.fail(error);
    }


    public static <T> Result<T> of(T result) {
        return new Result<>(true, result, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(false, null, message);
    }
}
