package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class ClientInfoMessage extends AmMessage {
    public String modVersion;
    public String playerUUID;
    public String playerName;


    public ClientInfoMessage(AmReader reader) {
        modVersion = reader.readString();
        playerUUID = reader.readString();
        playerName = reader.readString();
    }

    public ClientInfoMessage(
            String modVersion,
            String playerUUID,
            String playerName
    ) {
        this.modVersion = modVersion;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(modVersion);
        writer.writeString(playerUUID);
        writer.writeString(playerName);
    }
}
