package me.molybdenum.ambience_mini.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface IAmbienceMiniPacket extends CustomPacketPayload
{
    void handle(IPayloadContext context);
}