package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class SuccessMessage extends AmMessage {
    public SuccessMessage() { }

    public SuccessMessage(int handlerID) {
        this.handlerID = handlerID;
    }


    @Override
    public void writeTo(AmWriter writer) { }

    @Override
    public void readFrom(AmReader reader) { }
}
