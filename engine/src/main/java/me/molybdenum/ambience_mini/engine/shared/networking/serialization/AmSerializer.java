package me.molybdenum.ambience_mini.engine.shared.networking.serialization;

import me.molybdenum.ambience_mini.engine.shared.networking.MessageRegistry;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;

public abstract class AmSerializer implements AmWriter, AmReader
{
    public void serialize(AmMessage message) {
        writeInt(MessageRegistry.idFromClass(message.getClass()));
        message.writeTo(this);
    }

    public AmMessage deserialize() {
        AmMessage message = MessageRegistry.createFromId(readInt());
        message.readFrom(this);
        return message;
    }
}
