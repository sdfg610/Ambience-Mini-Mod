package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;

import java.util.UUID;

public class PutNameCacheMessage extends AmMessage {
    public String playerUuid;
    public String playerName;


    public PutNameCacheMessage() { }

    public PutNameCacheMessage(String playerUuid, String playerName) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(playerUuid);
        writer.writeString(playerName);
    }

    @Override
    public void readFrom(AmReader reader) {
        playerUuid = reader.readString();
        playerName = reader.readString();
    }
}
