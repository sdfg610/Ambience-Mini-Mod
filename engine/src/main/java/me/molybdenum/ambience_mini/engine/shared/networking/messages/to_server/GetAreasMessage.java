package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class GetAreasMessage extends AmMessage
{
    public GetAreasMessage(AmReader reader) { }

    public GetAreasMessage() { }

    @Override
    public void writeTo(AmWriter writer) { }
}
