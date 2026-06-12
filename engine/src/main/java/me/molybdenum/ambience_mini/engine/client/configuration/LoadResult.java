package me.molybdenum.ambience_mini.engine.client.configuration;

import me.molybdenum.ambience_mini.engine.client.configuration.messages.Message;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.Interpreter;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LoadResult {
    private final boolean isSuccess;

    public final Interpreter interpreter;
    public final List<Message> messages;


    private LoadResult(boolean isSuccess, Interpreter interpreter, List<Message> messages) {
        this.isSuccess = isSuccess;
        this.interpreter = interpreter;
        this.messages = messages;
    }


    public void match(BiConsumer<Interpreter, List<Message>> onSuccess, Consumer<List<Message>> onFailure) {
        if (isSuccess)
            onSuccess.accept(interpreter, messages);
        else
            onFailure.accept(messages);
    }


    public static <T> LoadResult of(Interpreter interpreter, List<Message> warnings) {
        return new LoadResult(true, interpreter, warnings);
    }

    public static <T> LoadResult fail(List<Message> messages) {
        return new LoadResult(false, null, messages);
    }
}
