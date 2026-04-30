package me.molybdenum.ambience_mini.v1_21_1.network;

import io.netty.buffer.ByteBuf;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.Result;
import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;


public record ToClientMessage(Result<AmMessage> message) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToClientMessage> TYPE
            = new CustomPacketPayload.Type<>(AmbienceMini.rl("to_client_message"));

    public static final StreamCodec<ByteBuf, ToClientMessage> STREAM_CODEC =
            StreamCodec.ofMember(ToClientMessage::encode, ToClientMessage::new);


    public ToClientMessage(AmMessage message) {
        this(Result.of(message));
    }

    public ToClientMessage(ByteBuf buffer) {
        this(new MessageSerializer(buffer).deserialize());
    }


    public void encode(ByteBuf buffer) {
        if (message.isSuccess())
            new MessageSerializer(buffer).serialize(message.value);
        else
            throw new RuntimeException("Tried to encode AmMessage from null result. This should never happen...");
    }

    public void handle(IPayloadContext context) {
        AmbienceMini.clientCore.networkManager.handleMessage(message);
    }


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
