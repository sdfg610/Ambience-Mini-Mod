package me.molybdenum.ambience_mini.network;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.network.to_client.MobTargetUpdateMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Function;

public class Networking {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder.
            named(AmbienceMini.rl(Common.MOD_ID))
            .clientAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION))
            .serverAcceptedVersions(NetworkRegistry.acceptMissingOr(PROTOCOL_VERSION))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    private static int id = 0;


    public static void initialize() {
        registerToClient(MobTargetUpdateMessage.class, MobTargetUpdateMessage::decode);
    }

    @SuppressWarnings("SameParameterValue")
    private static <MSG extends Message> void registerToClient(
            Class<MSG> messageType,
            Function<FriendlyByteBuf, MSG> decode
    ) {
        INSTANCE.registerMessage(
                id++, messageType, Message::encode, decode, Message::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }


    public static <MSG> void sendTo(MSG message, ServerPlayer player) {
        INSTANCE.sendTo(
                message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT
        );
    }
}
