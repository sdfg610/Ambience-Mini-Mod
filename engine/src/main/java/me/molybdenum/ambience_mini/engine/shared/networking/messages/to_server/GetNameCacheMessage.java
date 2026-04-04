package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;


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
