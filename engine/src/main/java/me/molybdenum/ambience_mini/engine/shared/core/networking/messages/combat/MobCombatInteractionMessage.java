package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class MobCombatInteractionMessage extends AmMessage {
    public final int entityID;


    public MobCombatInteractionMessage(AmReader reader) {
        entityID = reader.readInt();
    }

    public MobCombatInteractionMessage(int entityID) {
        this.entityID = entityID;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeInt(entityID);
    }
}
