package me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values;

import me.molybdenum.ambience_mini.engine.shared.music.Music;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

public final class PlaylistVal extends Value<List<Music>>
{
    public PlaylistVal() {
        super(null);
    }

    public PlaylistVal(List<Music> value) {
        super(value);
    }


    public List<Music> getValue() {
        return value;
    }


    @Override
    public String toStringInner(@NotNull List<Music> value) {
        return "[ " + String.join(", ", value.stream().map(music -> '"' + music.toString() + '"').toList()) + " ]";
    }

    @Override
    public boolean equals(Value<?> other) {
        return (value == null && other.value == null)
                || (value != null && other instanceof PlaylistVal playlistVal && playlistVal.value != null && compare(playlistVal));
    }

    private boolean compare(PlaylistVal playlistVal) {
        Iterator<Music> it1 = value.iterator();
        Iterator<Music> it2 = playlistVal.value.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            if (!it1.next().equals(it2.next()))
                return false;
        }

        return it1.hasNext() == it2.hasNext(); // True when both are false; meaning same length.
    }
}
