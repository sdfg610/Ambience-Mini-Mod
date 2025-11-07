package me.molybdenum.ambience_mini.network.to_client;

import io.netty.buffer.ByteBuf;
import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.network.IAmbienceMiniPacket;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record HasServerSupportPacket() implements IAmbienceMiniPacket {
    public static final CustomPacketPayload.Type<HasServerSupportPacket> TYPE
            = new CustomPacketPayload.Type<>(AmbienceMini.rl("has_server_support"));

    public static final StreamCodec<ByteBuf, HasServerSupportPacket> STREAM_CODEC
            = StreamCodec.unit(new HasServerSupportPacket());


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public void handle(final IPayloadContext context) {
        AmbienceMini.hasServerSupport = true;
    }
}
