package me.molybdenum.ambience_mini.network.to_client;

import me.molybdenum.ambience_mini.network.Message;
import me.molybdenum.ambience_mini.network.Networking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record MobTargetUpdateMessage(int entityID, boolean isTargetingPlayer) implements Message
{
    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.capacity(5);
        buffer.writeInt(entityID);
        buffer.writeBoolean(isTargetingPlayer);
    }

    public static MobTargetUpdateMessage decode(FriendlyByteBuf buffer) {
        return new MobTargetUpdateMessage(buffer.readInt(), buffer.readBoolean());
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        if (isTargetingPlayer)
            Networking.combatState.tryAddCombatantById(entityID, false);
        else
            Networking.combatState.removeCombatant(entityID);
        context.get().setPacketHandled(true);
    }
}
