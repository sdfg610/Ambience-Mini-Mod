package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class SuccessMessage extends AmMessage {
    public SuccessMessage(AmReader reader) { }

    public SuccessMessage(int handlerID) {
        this.handlerID = handlerID;
    }


    @Override
    public void writeTo(AmWriter writer) { }
}
