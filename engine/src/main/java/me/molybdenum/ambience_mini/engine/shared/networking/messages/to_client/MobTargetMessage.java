package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

public class MobTargetMessage extends AmMessage {
    public final int entityID;
    public final boolean isTargetingPlayer;


    public MobTargetMessage(AmReader reader) {
        entityID = reader.readInt();
        isTargetingPlayer = reader.readBoolean();
    }

    public MobTargetMessage(int entityID, boolean isTargetingPlayer) {
        this.entityID = entityID;
        this.isTargetingPlayer = isTargetingPlayer;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeInt(entityID);
        writer.writeBoolean(isTargetingPlayer);
    }
}
