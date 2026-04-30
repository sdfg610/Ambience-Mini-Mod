package me.molybdenum.ambience_mini.v1_19_2.network;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.utils.Result;
import me.molybdenum.ambience_mini.v1_19_2.AmbienceMini;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ToClientMessage(Result<AmMessage> message)
{
    public ToClientMessage(AmMessage message) {
        this(Result.of(message));
    }

    public void encode(FriendlyByteBuf buffer) {
        if (message.isSuccess())
            new MessageSerializer(buffer).serialize(message.value);
        else
            throw new RuntimeException("Tried to encode AmMessage from null result. This should never happen...");
    }

    public static ToClientMessage decode(FriendlyByteBuf buffer) {
        return new ToClientMessage(new MessageSerializer(buffer).deserialize());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        AmbienceMini.clientCore.networkManager.handleMessage(message);
        context.get().setPacketHandled(true);
    }
}
