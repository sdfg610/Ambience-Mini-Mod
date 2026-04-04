package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;

public class FailureMessage extends AmMessage {
    public final String translationKey;
    public final List<String> arguments;


    public FailureMessage(AmReader reader) {
        this.translationKey = reader.readString();
        this.arguments = reader.readStringList();
    }

    public FailureMessage(int handlerId, String translationKey, String... arguments) {
        this.handlerID = handlerId;
        this.translationKey = translationKey;
        this.arguments = Arrays.stream(arguments).toList();
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(translationKey);
        writer.writeStringList(arguments);
    }
}
