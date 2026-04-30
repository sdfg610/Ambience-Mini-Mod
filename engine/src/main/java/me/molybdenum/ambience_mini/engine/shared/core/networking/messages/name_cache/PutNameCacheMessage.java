package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.name_cache;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class PutNameCacheMessage extends AmMessage {
    public String playerUuid;
    public String playerName;


    public PutNameCacheMessage(AmReader reader) {
        playerUuid = reader.readString();
        playerName = reader.readString();
    }

    public PutNameCacheMessage(String playerUuid, String playerName) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(playerUuid);
        writer.writeString(playerName);
    }
}
