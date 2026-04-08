package me.molybdenum.ambience_mini.v1_21_1.network;

import io.netty.buffer.ByteBuf;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.Result;
import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ToServerMessage(Result<AmMessage> message) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<ToServerMessage> TYPE
            = new CustomPacketPayload.Type<>(AmbienceMini.rl("to_server_message"));

    public static final StreamCodec<ByteBuf, ToServerMessage> STREAM_CODEC =
            StreamCodec.ofMember(ToServerMessage::encode, ToServerMessage::new);



    public ToServerMessage(AmMessage message) {
        this(Result.of(message));
    }

    public ToServerMessage(ByteBuf buffer) {
        this(new MessageSerializer(buffer).deserialize());
    }


    public void encode(ByteBuf buffer) {
        if (message.isSuccess())
            new MessageSerializer(buffer).serialize(message.value);
        else
            throw new RuntimeException("Tried to encode AmMessage from null result. This should never happen...");
    }

    public void handle(IPayloadContext context) {
        AmbienceMini.serverCore.networkManager.handleMessage(message, (ServerPlayer)context.player());
    }


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
