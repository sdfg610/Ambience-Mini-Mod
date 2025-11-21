package me.molybdenum.ambience_mini.network;

import me.molybdenum.ambience_mini.core.state.CombatState;
import me.molybdenum.ambience_mini.network.to_client.MobTargetUpdatePacket;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;


public class Networking
{
    @OnlyIn(Dist.CLIENT)
    public static CombatState combatState;


    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional()
                .playToClient(MobTargetUpdatePacket.TYPE, MobTargetUpdatePacket.STREAM_CODEC, IAmbienceMiniPacket::handle);
    }
}
