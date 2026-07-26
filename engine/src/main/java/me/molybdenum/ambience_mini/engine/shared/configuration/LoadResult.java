package me.molybdenum.ambience_mini.engine.shared.configuration;

import me.molybdenum.ambience_mini.engine.shared.configuration.messages.Message;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LoadResult<T> {
    private final T result;
    private final List<Message> messages;


    private LoadResult(T result, List<Message> messages) {
        this.result = result;
        this.messages = messages;
    }


    public void match(BiConsumer<T, List<Message>> onSuccess, Consumer<List<Message>> onFailure) {
        if (result == null)
            onFailure.accept(messages);
        else
            onSuccess.accept(result, messages);
    }


    public static <T> LoadResult<T> of(T result, List<Message> warnings) {
        return new LoadResult<T>(result, warnings);
    }

    public static <T> LoadResult<T> fail(List<Message> messages) {
        return new LoadResult<T>(null, messages);
    }
}
