package me.molybdenum.ambience_mini.engine.shared.utils.results;

import java.util.function.Function;


public class StrResult<T> {
    private final boolean isSuccess;

    public final T value;
    public final String error;


    private StrResult(boolean isSuccess, T value, String error) {
        this.isSuccess = isSuccess;
        this.value = value;
        this.error = error;
    }


    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean isFailure() {
        return !isSuccess;
    }


    public T getValue() {
        if (isSuccess)
            return value;
        throw new IllegalStateException("Tried to get value of failure-result.");
    }

    public <E> StrResult<E> map(Function<T, E> onResult) {
        return isSuccess
                ? StrResult.of(onResult.apply(value))
                : StrResult.fail(error);
    }


    public static <T> StrResult<T> of(T result) {
        return new StrResult<>(true, result, null);
    }

    public static <T> StrResult<T> fail(String message) {
        return new StrResult<>(false, null, message);
    }
}
