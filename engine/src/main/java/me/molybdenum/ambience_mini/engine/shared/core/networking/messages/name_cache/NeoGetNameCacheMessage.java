package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.name_cache;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;


public class NeoGetNameCacheMessage extends AmMessage {
    public String playerUuid;


    public NeoGetNameCacheMessage(AmReader reader) {
        playerUuid = reader.readString();
    }

    public NeoGetNameCacheMessage(String playerUuid) {
        this.playerUuid = playerUuid;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(playerUuid);
    }
}
