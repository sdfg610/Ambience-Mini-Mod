package me.molybdenum.ambience_mini.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public interface Message {
    void encode(FriendlyByteBuf buffer);
    void handle(Supplier<NetworkEvent.Context> context);
}
