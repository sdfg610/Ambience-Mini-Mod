package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class MobTargetMessage implements AmMessage {
    public int entityID;
    public boolean isTargetingPlayer;


    public MobTargetMessage() { }

    public MobTargetMessage(int entityID, boolean isTargetingPlayer) {
        this.entityID = entityID;
        this.isTargetingPlayer = isTargetingPlayer;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeInt(entityID);
        writer.writeBoolean(isTargetingPlayer);
    }

    @Override
    public void readFrom(AmReader reader) {
        entityID = reader.readInt();
        isTargetingPlayer = reader.readBoolean();
    }
}
