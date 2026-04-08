package me.molybdenum.ambience_mini.v1_21_1.network;

import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.v1_21_1.network.configuration.ServerAmVersionPacket;
import me.molybdenum.ambience_mini.v1_21_1.network.configuration.ServerSupportConfigurationTask;
import me.molybdenum.ambience_mini.v1_21_1.network.configuration.VersionAckPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;


public class Networking
{
    public static void registerTasks(final RegisterConfigurationTasksEvent event) {
        event.register(new ServerSupportConfigurationTask(event.getListener()));
    }

    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        event.registrar(Common.PROTOCOL_VERSION).optional()
                .configurationToClient(ServerAmVersionPacket.TYPE, ServerAmVersionPacket.STREAM_CODEC, ServerAmVersionPacket::handle)
                .configurationToServer(VersionAckPacket.TYPE, VersionAckPacket.STREAM_CODEC, VersionAckPacket::handle)
                .playToClient(ToClientMessage.TYPE, ToClientMessage.STREAM_CODEC, ToClientMessage::handle)
                .playToServer(ToServerMessage.TYPE, ToServerMessage.STREAM_CODEC, ToServerMessage::handle);
    }


    public static <MSG extends AmMessage> void sendToPlayer(MSG message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ToClientMessage(message));
    }

    public static <MSG extends AmMessage> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(new ToServerMessage(message));
    }
}
