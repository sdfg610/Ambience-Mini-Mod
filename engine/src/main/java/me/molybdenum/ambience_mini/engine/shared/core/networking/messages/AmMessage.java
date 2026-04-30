package me.molybdenum.ambience_mini.engine.shared.core.networking.messages;

import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.FailureMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.SuccessMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;


public abstract class AmMessage implements AmSerializable {
    public int handlerID = Integer.MIN_VALUE;


    public boolean hasHandler() {
        return handlerID != Integer.MIN_VALUE;
    }


    public FailureMessage failure(AmLang key) {
        return failure(Text.ofTranslatable(key));
    }

    public FailureMessage failure(Text message) {
        return new FailureMessage(handlerID, message);
    }


    public SuccessMessage success() {
        return new SuccessMessage(handlerID);
    }
}
