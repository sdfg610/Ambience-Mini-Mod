package me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmWriter;


public class GetNameCacheMessage extends AmMessage {
    public String playerUuid;


    public GetNameCacheMessage() { }

    public GetNameCacheMessage(String playerUuid) {
        this.playerUuid = playerUuid;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(playerUuid);
    }

    @Override
    public void readFrom(AmReader reader) {
        playerUuid = reader.readString();
    }
}
