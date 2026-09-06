package me.molybdenum.ambience_mini.engine.server.core.music;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists.PlaylistGroup;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.BaseInterpreter;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.Config;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists.PlaylistInstance;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.server_playlists.ServerPlaylists;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.VariableEnv;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.PlaylistVal;
import me.molybdenum.ambience_mini.engine.shared.music.music_dto.MusicDTO;
import me.molybdenum.ambience_mini.engine.shared.music.music_dto.PlaylistDTO;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ServerConfigInterpreter extends BaseInterpreter
{
    private final ServerPlaylists serverPlaylists;
    private final BaseMusicProvider musicProvider;


    public ServerConfigInterpreter(Config config, BaseMusicProvider musicProvider) {
        super(null);

        this.serverPlaylists = config.serverPlaylists();
        this.musicProvider = musicProvider;
    }

    public ArrayList<PlaylistDTO> loadServerPlaylists() throws FileNotFoundException {
        ArrayList<PlaylistDTO> playlistDTOs = new ArrayList<>();
        if (serverPlaylists != null)
            loadServerPlaylists(serverPlaylists, playlistDTOs, "");
        return playlistDTOs;
    }


    private void loadServerPlaylists(ServerPlaylists playlists, ArrayList<PlaylistDTO> serverPlaylists, String groupName) throws FileNotFoundException {
        if (playlists instanceof PlaylistInstance inst) {
            var musicList = ((PlaylistVal)evalExpr(inst.expr(), null)).getValue(); // Static analysis ensures we get playlist here.

            var musicDTOs = new ArrayList<MusicDTO>(musicList.size());
            for (var music : musicList)
                musicDTOs.add(new MusicDTO(music, musicProvider));

            serverPlaylists.add(new PlaylistDTO(groupName, musicDTOs));
        }
        else if (playlists instanceof PlaylistGroup plGroup) {
            for (var pl : plGroup.body().entrySet())
                loadServerPlaylists(pl.getValue(), serverPlaylists, groupName.isEmpty() ? pl.getKey() : groupName + "." + pl.getKey());
        }
    }
}
