package me.molybdenum.ambience_mini.engine.shared.networking.serialization;

import me.molybdenum.ambience_mini.engine.shared.networking.MessageRegistry;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.Result;

public abstract class AmSerializer implements AmWriter, AmReader
{
    public void serialize(AmMessage message) {
        writeInt(MessageRegistry.idFromClass(message.getClass()));
        writeInt(message.handlerID);
        message.writeTo(this);
    }

    public Result<AmMessage> deserialize() {
        return MessageRegistry.createFromId(readInt()).map(message -> {
            message.handlerID = readInt();
            message.readFrom(this);
            return message;
        });
    }
}
