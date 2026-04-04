package me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional;

import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class PutAreaMessage extends AmMessage {
    public final Area area;
    public final boolean overwriteIfExists;


    public PutAreaMessage(AmReader reader) {
        this.area = new Area(reader);
        this.overwriteIfExists = reader.readBoolean();
    }

    public PutAreaMessage(Area area) {
        this.area = area;
        this.overwriteIfExists = true;
    }

    public PutAreaMessage(Area area, boolean overwriteIfExists) {
        this.area = area;
        this.overwriteIfExists = overwriteIfExists;
    }


    @Override
    public void writeTo(AmWriter writer) {
        area.writeTo(writer);
        writer.writeBoolean(overwriteIfExists);
    }
}
