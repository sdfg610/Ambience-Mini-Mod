package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class IndirectAttackOnMobMessage extends AmMessage {
    public final int entityID;


    public IndirectAttackOnMobMessage(AmReader reader) {
        entityID = reader.readInt();
    }

    public IndirectAttackOnMobMessage(int entityID) {
        this.entityID = entityID;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeInt(entityID);
    }
}
