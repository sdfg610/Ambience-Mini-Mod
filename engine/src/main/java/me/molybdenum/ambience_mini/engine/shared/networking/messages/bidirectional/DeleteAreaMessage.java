package me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class DeleteAreaMessage extends AmMessage {
    public int areaId;


    public DeleteAreaMessage() { }

    public DeleteAreaMessage(int areaId) {
        this.areaId = areaId;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeInt(areaId);
    }

    @Override
    public void readFrom(AmReader reader) {
        this.areaId = reader.readInt();
    }
}
