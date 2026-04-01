package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class RequestAreasMessage extends AmMessage
{
    public RequestAreasMessage() { }

    @Override
    public void writeTo(AmWriter writer) { }

    @Override
    public void readFrom(AmReader reader) { }
}
