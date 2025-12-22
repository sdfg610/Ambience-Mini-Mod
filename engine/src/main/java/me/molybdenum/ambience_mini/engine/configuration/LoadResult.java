package me.molybdenum.ambience_mini.engine.configuration;

import me.molybdenum.ambience_mini.engine.configuration.errors.LoadError;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.Interpreter;

import java.util.List;
import java.util.function.Consumer;

public class LoadResult {
    private final boolean isSuccess;

    public final Interpreter interpreter;
    public final List<LoadError> errors;


    private LoadResult(boolean isSuccess, Interpreter interpreter, List<LoadError> errors) {
        this.isSuccess = isSuccess;
        this.interpreter = interpreter;
        this.errors = errors;
    }


    public void match(Consumer<Interpreter> onSuccess, Consumer<List<LoadError>> onFailure) {
        if (isSuccess)
            onSuccess.accept(interpreter);
        else
            onFailure.accept(errors);
    }


    public static <T> LoadResult of(Interpreter interpreter) {
        return new LoadResult(true, interpreter, null);
    }

    public static <T> LoadResult fail(List<LoadError> message) {
        return new LoadResult(false, null, message);
    }
}
