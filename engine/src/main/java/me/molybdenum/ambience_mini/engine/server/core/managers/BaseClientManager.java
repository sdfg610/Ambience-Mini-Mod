package me.molybdenum.ambience_mini.engine.server.core.managers;

import me.molybdenum.ambience_mini.engine.shared.utils.AmVersion;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseClientManager<TServerPlayer>
{
    private final Object _lock = new Object();
    private final Map<TServerPlayer, AmVersion> _playerToModVersion = new HashMap<>();


    public void setPlayer(TServerPlayer player, AmVersion version) {
        synchronized (_lock) {
            _playerToModVersion.put(player, version);
        }
    }

    public void renewPlayer(TServerPlayer oldPlayer, TServerPlayer newPlayer) {
        synchronized (_lock) {
            var info = _playerToModVersion.remove(oldPlayer);
            if (info != null)
                _playerToModVersion.put(newPlayer, info);
        }
    }

    public void removePlayer(TServerPlayer player) {
        synchronized (_lock) {
            _playerToModVersion.remove(player);
        }
    }


    public AmVersion getModVersion(TServerPlayer player) {
        synchronized (_lock) {
            return _playerToModVersion.getOrDefault(player, AmVersion.ZERO);
        }
    }
}
