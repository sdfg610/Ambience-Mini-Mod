package me.molybdenum.ambience_mini.v1_20_1.network;

import me.molybdenum.ambience_mini.v1_20_1.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.Result;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ToServerMessage(Result<AmMessage> message)
{
    public ToServerMessage(AmMessage message) {
        this(Result.of(message));
    }

    public void encode(FriendlyByteBuf buffer) {
        if (message.isSuccess())
            new MessageSerializer(buffer).serialize(message.value);
        else
            throw new RuntimeException("Tried to encode AmMessage from null result. This should never happen...");
    }

    public static ToServerMessage decode(FriendlyByteBuf buffer) {
        return new ToServerMessage(new MessageSerializer(buffer).deserialize());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        AmbienceMini.serverCore.networkManager.handleMessage(message, context.get().getSender());
        context.get().setPacketHandled(true);
    }
}
