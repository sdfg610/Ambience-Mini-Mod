package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.name_cache;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;


public class GetNameCacheMessage extends AmMessage {
    public String playerUuid;


    public GetNameCacheMessage(AmReader reader) {
        playerUuid = reader.readString();
    }

    public GetNameCacheMessage(String playerUuid) {
        this.playerUuid = playerUuid;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(playerUuid);
    }
}
