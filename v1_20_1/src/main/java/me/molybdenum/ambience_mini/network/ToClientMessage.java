package me.molybdenum.ambience_mini.network;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ToClientMessage(AmMessage message)
{
    public void encode(FriendlyByteBuf buffer) {
        new MessageSerializer(buffer).serialize(message);
    }

    public static ToClientMessage decode(FriendlyByteBuf buffer) {
        return new ToClientMessage(new MessageSerializer(buffer).deserialize());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        AmbienceMini.clientCore.networkManager.handleMessage(message);
        context.get().setPacketHandled(true);
    }
}
