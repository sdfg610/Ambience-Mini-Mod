package me.molybdenum.ambience_mini.engine.client.core.networking.handlers;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.responses.Response;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AsyncHandler implements Handler {
    @Nullable
    private final Consumer<byte[]> onSuccess;
    @Nullable
    private final Consumer<Text> onFailure;
    @Nullable
    private final Runnable onTimeout;


    public AsyncHandler(
            @Nullable Consumer<byte[]> onSuccess,
            @Nullable Consumer<Text> onFailure,
            @Nullable Runnable onTimeout
    ) {
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
        this.onTimeout = onTimeout;
    }


    @Override
    public void handle(Response response) {
        if (response == null) {
            if (onTimeout != null)
                onTimeout.run();
        }
        else if (response.isSuccess()) {
            if (onSuccess != null)
                onSuccess.accept(response.data);
        }
        else {
            if (onFailure != null)
                onFailure.accept(response.error);
        }
    }
}
