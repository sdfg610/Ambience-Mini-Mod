package me.molybdenum.ambience_mini.network.to_client;

import io.netty.buffer.ByteBuf;
import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.network.IAmbienceMiniPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record MobTargetUpdatePacket(int entityID, boolean isTargetingPlayer) implements IAmbienceMiniPacket
{
    public static final CustomPacketPayload.Type<MobTargetUpdatePacket> TYPE
            = new CustomPacketPayload.Type<>(AmbienceMini.rl("mob_target_update"));

    public static final StreamCodec<ByteBuf, MobTargetUpdatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MobTargetUpdatePacket::entityID,
            ByteBufCodecs.BOOL, MobTargetUpdatePacket::isTargetingPlayer,
            MobTargetUpdatePacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        if (isTargetingPlayer)
            AmbienceMini.combat().tryAddCombatantById(entityID, false);
        else
            AmbienceMini.combat().removeCombatant(entityID);
    }
}
