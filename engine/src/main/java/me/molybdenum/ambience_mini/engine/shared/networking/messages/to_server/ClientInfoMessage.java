package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

import java.util.UUID;

public class ClientInfoMessage extends AmMessage {
    public String modVersion;
    public String playerUUID;
    public String playerName;


    public ClientInfoMessage() { }

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

    @Override
    public void readFrom(AmReader reader) {
        modVersion = reader.readString();
        playerUUID = reader.readString();
        playerName = reader.readString();
    }
}
