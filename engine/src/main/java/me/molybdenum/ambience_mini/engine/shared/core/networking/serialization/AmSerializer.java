package me.molybdenum.ambience_mini.engine.shared.core.networking.serialization;

import me.molybdenum.ambience_mini.engine.shared.core.networking.MessageRegistry;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.results.StrResult;

public abstract class AmSerializer implements AmWriter, AmReader
{
    public void serialize(AmMessage message) {
        writeInt(MessageRegistry.idFromClass(message.getClass()));
        message.writeTo(this);
        writeInt(message.handlerID);
    }

    public StrResult<AmMessage> deserialize() {
        return MessageRegistry.createFromId(readInt(), this).map(message -> {
            message.handlerID = readInt();
            return message;
        });
    }
}
