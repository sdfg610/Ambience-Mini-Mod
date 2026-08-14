package me.molybdenum.ambience_mini.engine.client.core.networking.handlers;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.Response;

public interface Handler {
    void handle(Response response);
}
