package me.molybdenum.ambience_mini.server.core.managers;

import me.molybdenum.ambience_mini.engine.server.core.managers.BaseServerNetworkManager;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.network.Networking;
import net.minecraft.server.level.ServerPlayer;

public class ServerNetworkManager extends BaseServerNetworkManager<ServerPlayer>
{
    @Override
    public void sendToPlayer(AmMessage message, ServerPlayer serverPlayer) {
        Networking.sendToPlayer(message, serverPlayer);
    }
}
