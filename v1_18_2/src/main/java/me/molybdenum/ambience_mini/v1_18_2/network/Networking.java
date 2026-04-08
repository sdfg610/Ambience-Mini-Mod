package me.molybdenum.ambience_mini.v1_18_2.network;

import me.molybdenum.ambience_mini.v1_18_2.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;


public class Networking {
    private static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.
            named(AmbienceMini.rl(Common.MOD_ID))
            .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(Common.PROTOCOL_VERSION))
            .serverAcceptedVersions(NetworkRegistry.acceptMissingOr(Common.PROTOCOL_VERSION))
            .networkProtocolVersion(() -> Common.PROTOCOL_VERSION)
            .simpleChannel();


    public static void initialize() {
        INSTANCE.registerMessage(
                1, ToClientMessage.class, ToClientMessage::encode, ToClientMessage::decode, ToClientMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        INSTANCE.registerMessage(
                2, ToServerMessage.class, ToServerMessage::encode, ToServerMessage::decode, ToServerMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }


    public static <MSG extends AmMessage> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new ToClientMessage(message));
    }

    public static <MSG extends AmMessage> void sendToServer(MSG message) {
        INSTANCE.sendToServer(new ToServerMessage(message));
    }
}
