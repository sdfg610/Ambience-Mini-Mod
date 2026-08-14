package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.server_music;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class NotifyServerPlaylistCountMessage extends AmMessage {
    public int byteSize;
    public int playlistCount;


    public NotifyServerPlaylistCountMessage(int byteSize, int playlistCount) {
        this.byteSize = byteSize;
        this.playlistCount = playlistCount;
    }

    public NotifyServerPlaylistCountMessage(AmReader reader) {
        this(reader.readInt(), reader.readInt());
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeInt(byteSize);
        writer.writeInt(playlistCount);
    }
}
