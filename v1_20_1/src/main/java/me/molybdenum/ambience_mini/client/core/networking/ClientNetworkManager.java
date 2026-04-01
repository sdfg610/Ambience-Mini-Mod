package me.molybdenum.ambience_mini.client.core.networking;

import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.network.Networking;

public class ClientNetworkManager extends BaseClientNetworkManager {
    @Override
    protected void sendToServerInternal(AmMessage message) {
        Networking.sendToServer(message);
    }
}
