package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class FailureMessage implements AmMessage
{
    public String messageTranslationKey;  // A translatable overview of what went wrong
    public String errorDetails;           // Details of the error in English.


    public FailureMessage() { }

    public FailureMessage(String messageTranslationKey, String description) {
        this.messageTranslationKey = messageTranslationKey;
        this.errorDetails = description;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(messageTranslationKey);
        writer.writeString(errorDetails);
    }

    @Override
    public void readFrom(AmReader reader) {
        this.messageTranslationKey = reader.readString();
        this.errorDetails = reader.readString();
    }
}
