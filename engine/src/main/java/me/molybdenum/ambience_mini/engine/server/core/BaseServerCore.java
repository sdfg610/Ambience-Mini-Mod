package me.molybdenum.ambience_mini.engine.server.core;

import me.molybdenum.ambience_mini.engine.server.core.managers.BaseServerNetworkManager;
import me.molybdenum.ambience_mini.engine.server.core.managers.BaseClientManager;
import org.slf4j.Logger;

public abstract class BaseServerCore<
        TServerPlayer,
        TClientManager extends BaseClientManager<TServerPlayer>,
        TNetworkManager extends BaseServerNetworkManager<TServerPlayer>
> {
    // Utils
    public final Logger logger;

    // Setup
    public final TClientManager clientManager;

    // Networking
    public final TNetworkManager networkManager;


    public BaseServerCore(
            Logger logger,
            TClientManager clientManager,
            TNetworkManager networkManager
    ) {
        this.logger = logger;

        this.clientManager = clientManager;
        this.networkManager = networkManager;

        this.networkManager.init(this);
    }


    public void stop() {
        // TODO!!!!!!   E.g. save areas
    }
}
