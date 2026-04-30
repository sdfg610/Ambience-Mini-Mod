package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;

public class FailureMessage extends AmMessage {
    public final Text message;


    public FailureMessage(AmReader reader) {
        this.message = new Text(reader);
    }

    public FailureMessage(int handlerId, Text message) {
        this.handlerID = handlerId;
        this.message = message;
    }


    @Override
    public void writeTo(AmWriter writer) {
        message.writeTo(writer);
    }
}
