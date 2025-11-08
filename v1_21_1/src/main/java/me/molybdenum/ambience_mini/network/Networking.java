package me.molybdenum.ambience_mini.network;

import me.molybdenum.ambience_mini.network.to_client.MobTargetUpdatePacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;


public class Networking
{
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional()
                .playToClient(MobTargetUpdatePacket.TYPE, MobTargetUpdatePacket.STREAM_CODEC, IAmbienceMiniPacket::handle);
    }
}
