package me.molybdenum.ambience_mini.network;

import me.molybdenum.ambience_mini.network.configuration.ServerSupportConfigurationTask;
import me.molybdenum.ambience_mini.network.to_client.HasServerSupportPacket;
import me.molybdenum.ambience_mini.network.to_client.MobTargetUpdatePacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public class Networking
{
    public static void registerTasks(final RegisterConfigurationTasksEvent event) {
        event.register(new ServerSupportConfigurationTask(event.getListener()));
    }

    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional()
                .configurationToClient(HasServerSupportPacket.TYPE, HasServerSupportPacket.STREAM_CODEC, IAmbienceMiniPacket::handle)
                .playToClient(MobTargetUpdatePacket.TYPE, MobTargetUpdatePacket.STREAM_CODEC, IAmbienceMiniPacket::handle);
    }
}
