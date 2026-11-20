package me.molybdenum.ambience_mini.engine.shared.utils.results;

import me.molybdenum.ambience_mini.engine.shared.utils.Text;

import java.util.function.Function;


public class TextResult<T> {
    private final boolean isSuccess;

    public final T value;
    public final Text error;


    private TextResult(boolean isSuccess, T value, Text error) {
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

    public <E> TextResult<E> map(Function<T, E> onResult) {
        return isSuccess
                ? TextResult.of(onResult.apply(value))
                : TextResult.fail(error);
    }

    public <E> E match(Function<T, E> onSuccess, Function<Text, E> onError) {
        return isSuccess
                ? onSuccess.apply(value)
                : onError.apply(error);
    }


    public static <T> TextResult<T> of(T result) {
        return new TextResult<>(true, result, null);
    }

    public static <T> TextResult<T> fail(Text message) {
        return new TextResult<>(false, null, message);
    }
}
