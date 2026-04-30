package me.molybdenum.ambience_mini.v1_18_2.client.core.networking;

import me.molybdenum.ambience_mini.engine.client.core.networking.BaseClientNetworkManager;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.v1_18_2.network.Networking;

public class ClientNetworkManager extends BaseClientNetworkManager {
    @Override
    protected void sendToServerInternal(AmMessage message) {
        Networking.sendToServer(message);
    }
}
