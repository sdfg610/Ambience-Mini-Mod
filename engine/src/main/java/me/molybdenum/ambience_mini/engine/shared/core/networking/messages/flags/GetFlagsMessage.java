package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class GetFlagsMessage extends AmMessage
{
    public GetFlagsMessage(AmReader reader) { }

    public GetFlagsMessage() { }

    @Override
    public void writeTo(AmWriter writer) { }
}
