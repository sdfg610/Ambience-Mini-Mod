package me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional;

import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class PutAreaMessage extends AmMessage {
    public Area area;
    public boolean overwriteIfExists;


    public PutAreaMessage() {}

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

    @Override
    public void readFrom(AmReader reader) {
        this.area = reader.read(Area::new);
        this.overwriteIfExists = reader.readBoolean();
    }
}
