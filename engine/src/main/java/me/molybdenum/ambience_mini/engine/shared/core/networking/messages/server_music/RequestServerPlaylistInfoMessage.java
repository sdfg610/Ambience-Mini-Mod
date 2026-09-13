package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.server_music;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.helper.HelperReader;

public class RequestServerPlaylistInfoMessage extends AmMessage
{
    public RequestServerPlaylistInfoMessage() { }

    public RequestServerPlaylistInfoMessage(AmReader ignored) { }

    @Override
    public void writeTo(AmWriter writer) { }


    public Response noPlaylists() {
        return new Response(false, -1, -1);
    }

    public Response hasPlaylists(int byteSize, int playlistCount) {
        return new Response(true, byteSize, playlistCount);
    }

    public static Response parseResponse(byte[] data) {
        return new Response(new HelperReader(data));
    }


    public static class Response implements AmSerializable {
        public final boolean hasServerPlaylists;
        public final int byteSize;
        public final int playlistCount;


        protected Response(boolean hasServerPlaylists, int byteSize, int playlistCount) {
            this.hasServerPlaylists = hasServerPlaylists;
            this.byteSize = byteSize;
            this.playlistCount = playlistCount;
        }

        public Response(AmReader reader) {
            this(reader.readBoolean(), reader.readInt(), reader.readInt());
        }


        @Override
        public void writeTo(AmWriter writer) {
            writer.writeBoolean(hasServerPlaylists);
            writer.writeInt(byteSize);
            writer.writeInt(playlistCount);
        }
    }
}
