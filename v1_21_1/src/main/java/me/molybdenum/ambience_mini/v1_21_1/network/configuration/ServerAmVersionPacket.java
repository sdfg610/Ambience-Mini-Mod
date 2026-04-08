package me.molybdenum.ambience_mini.v1_21_1.network.configuration;

import io.netty.buffer.ByteBuf;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;
import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ServerAmVersionPacket(String amVersion) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerAmVersionPacket> TYPE
            = new CustomPacketPayload.Type<>(AmbienceMini.rl("has_server_support"));

    public static final StreamCodec<ByteBuf, ServerAmVersionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerAmVersionPacket::amVersion,
            ServerAmVersionPacket::new
    );


    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public void handle(final IPayloadContext context) {
        AmbienceMini.configuredAmVersion = AmVersion.ofString(amVersion);
        context.reply(new VersionAckPacket());
    }
}
