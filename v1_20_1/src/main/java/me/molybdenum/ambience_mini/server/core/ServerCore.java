package me.molybdenum.ambience_mini.server.core;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.server.core.managers.ClientManager;
import me.molybdenum.ambience_mini.server.core.managers.ServerNetworkManager;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public class ServerCore extends BaseServerCore<
        ServerPlayer, ClientManager, ServerNetworkManager
> {
    public ServerCore(
            Logger logger,
            ClientManager baseClientManager,
            ServerNetworkManager networkManager
    ) {
        super(logger, baseClientManager, networkManager);
    }
}
