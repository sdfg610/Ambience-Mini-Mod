package me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional;

import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.networking.MessageRegistry;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class PutAreaMessage implements AmMessage
{
    public Area area;


    public PutAreaMessage() {}

    public PutAreaMessage(Area area) {
        this.area = area;
    }


    @Override
    public void writeTo(AmWriter writer) {
        area.writeTo(writer);
    }

    @Override
    public void readFrom(AmReader reader) {
        this.area = reader.readTo(new Area());
    }
}
