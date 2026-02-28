package me.molybdenum.ambience_mini.engine.client.core.setup;

import me.molybdenum.ambience_mini.engine.shared.utils.AmVersion;

public class ServerSetup
{
    public AmVersion serverVersion;
    public boolean isOnLocalServer;


    public ServerSetup() {
        reset();
    }

    public void reset() {
        serverVersion = AmVersion.ZERO;
        isOnLocalServer = false;
    }
}
