package me.molybdenum.ambience_mini.v1_21_1.network.configuration;

import io.netty.buffer.ByteBuf;
import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VersionAckPacket() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<VersionAckPacket> TYPE
            = new CustomPacketPayload.Type<>(AmbienceMini.rl("version_ack_packet"));

    // Unit codec with no data to write
    public static final StreamCodec<ByteBuf, VersionAckPacket> STREAM_CODEC
            = StreamCodec.unit(new VersionAckPacket());


    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public void handle(final IPayloadContext context) {
        context.finishCurrentTask(ServerSupportConfigurationTask.TYPE);
    }
}
