package me.molybdenum.ambience_mini.v1_19_2.server.core.networking;

import me.molybdenum.ambience_mini.engine.server.core.networking.BaseServerNetworkManager;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.v1_19_2.network.Networking;
import net.minecraft.server.level.ServerPlayer;

public class ServerNetworkManager extends BaseServerNetworkManager<ServerPlayer>
{
    @Override
    public void sendToPlayer(AmMessage message, ServerPlayer serverPlayer) {
        Networking.sendToPlayer(message, serverPlayer);
    }

    @Override
    protected String getServerPlayerUUID(ServerPlayer serverPlayer) {
        return serverPlayer.getGameProfile().getId().toString();
    }
}
