package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class GetAreasMessage extends AmMessage
{
    public GetAreasMessage(AmReader reader) { }

    public GetAreasMessage() { }

    @Override
    public void writeTo(AmWriter writer) { }
}
