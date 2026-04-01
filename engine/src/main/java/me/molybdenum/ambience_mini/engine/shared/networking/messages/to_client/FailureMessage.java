package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

import java.util.logging.Handler;

public class FailureMessage extends AmMessage {
    public String translationKey;
    public String[] arguments;


    public FailureMessage() { }

    public FailureMessage(int handlerId, String translationKey, String... arguments) {
        this.handlerID = handlerId;
        this.translationKey = translationKey;
        this.arguments = arguments;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(translationKey);
        writer.writeInt(arguments.length);
        for (var arg : arguments)
            writer.writeString(arg);
    }

    @Override
    public void readFrom(AmReader reader) {
        this.translationKey = reader.readString();
        int length = reader.readInt();
        this.arguments = new String[length];
        for (int i = 0; i < length; i++) {
            this.arguments[i] = reader.readString();
        }
    }
}
