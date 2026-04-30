package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas;

import me.molybdenum.ambience_mini.engine.shared.core.areas.Area;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class CreateAreaMessage extends AmMessage {
    public final Area area;


    public CreateAreaMessage(AmReader reader) {
        this.area = new Area(reader);
    }

    public CreateAreaMessage(Area area) {
        this.area = area;
    }


    @Override
    public void writeTo(AmWriter writer) {
        area.writeTo(writer);
    }
}