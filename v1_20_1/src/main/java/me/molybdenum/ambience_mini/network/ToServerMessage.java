package me.molybdenum.ambience_mini.network;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ToServerMessage(AmMessage message)
{
    public void encode(FriendlyByteBuf buffer) {
        new MessageSerializer(buffer).serialize(message);
    }

    public static ToServerMessage decode(FriendlyByteBuf buffer) {
        return new ToServerMessage(new MessageSerializer(buffer).deserialize());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        AmbienceMini.serverCore.networkManager.handleMessage(message, context.get().getSender());
        context.get().setPacketHandled(true);
    }
}
