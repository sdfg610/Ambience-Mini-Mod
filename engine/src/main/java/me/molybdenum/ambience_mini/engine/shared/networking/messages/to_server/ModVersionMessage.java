package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class ModVersionMessage implements AmMessage
{
    public String modVersion;


    public ModVersionMessage() { }

    public ModVersionMessage(String modVersion) {
        this.modVersion = modVersion;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(modVersion);
    }

    @Override
    public void readFrom(AmReader reader) {
        modVersion = reader.readString();
    }
}
