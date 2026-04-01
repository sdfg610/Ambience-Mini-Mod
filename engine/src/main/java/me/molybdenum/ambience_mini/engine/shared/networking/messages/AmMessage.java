package me.molybdenum.ambience_mini.engine.shared.networking.messages;

import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.FailureMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.SuccessMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmSerializable;


public abstract class AmMessage implements AmSerializable {
    public int handlerID = Integer.MIN_VALUE;


    public boolean hasHandler() {
        return handlerID != Integer.MIN_VALUE;
    }


    public FailureMessage failure(AmLang translationKey, String... arguments) {
        return failure(translationKey.key, arguments);
    }

    public FailureMessage failure(String translationKey, String... arguments) {
        return new FailureMessage(handlerID, translationKey, arguments);
    }


    public SuccessMessage success() {
        return new SuccessMessage(handlerID);
    }
}
