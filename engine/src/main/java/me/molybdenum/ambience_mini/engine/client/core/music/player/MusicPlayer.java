package me.molybdenum.ambience_mini.engine.client.core.music.player;

import me.molybdenum.ambience_mini.engine.shared.music.Music;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;
import me.molybdenum.ambience_mini.engine.client.core.music.decoders.AmDecoder;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;

public class MusicPlayer {
    private final Stack<Pair<Integer, Channel>> stack = new Stack<>(); // Priorities and channels

    private final BaseMusicProvider musicProvider;
    private final Runnable onRanToEnd;

    private float currentMusicVolume;

    private final List<Consumer<NowPlaying>> nowPlayingListeners = new ArrayList<>();


    public MusicPlayer(BaseMusicProvider musicProvider, Runnable onRanToEnd) {
        this.musicProvider = musicProvider;
        this.onRanToEnd = onRanToEnd;
    }


    public List<String> getActiveSoundtracks() {
        synchronized (stack) {
            return stack.stream()
                    .map(pair -> pair.right().music.path())
                    .toList();
        }
    }


    public void updateBuffers() {
        synchronized (stack) {
            if (stack.empty())
                return;

            var channel = stack.peek().right();
            channel.updateBuffers();
            if (channel.isStopped()) {
                channel.stopAndClose(false);
                onRanToEnd.run();
            }
        }
    }

    public void setVolume(float volume) {
        if (Math.abs(currentMusicVolume - volume) >= 0.01f) {
            currentMusicVolume = volume;
            for (var pair : stack)
                pair.right().setVolume(volume);
        }
    }


    @Nullable
    public Music getMusicByPriority(int priority) {
        synchronized (stack) {
            for (var pair : stack)
                if (pair.left() == priority)
                    return pair.right().music;
        }
        return null;
    }

    public Optional<Integer> getTopPriority() {
        synchronized (stack) {
            return stack.empty() ? Optional.empty() : Optional.of(stack.peek().left());
        }
    }


    public void play(int priority, Music music, boolean doFade) {
        if (priority < 0)
            throw new RuntimeException("Priority of music cannot be negative!");

        Channel channel;
        synchronized (stack) {
            try {
                stopAllAbove(priority - 1, doFade);
                channel = createChannel(music, currentMusicVolume);
                stack.push(new Pair<>(priority, channel));
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File '" + music.path() + "' not found. Fix your Ambience config!", e);
            }
        }
        innerResume(channel, doFade);
    }

    public void stopAllAbove(int priority, boolean doFade) {
        synchronized (stack) {
            while (!stack.empty()) {
                if (stack.peek().left() > priority)
                    stack.pop().right().stopAndClose(doFade);
                else
                    break;
            }
        }
    }

    private Channel createChannel(Music music, float volume) throws FileNotFoundException {
        try {
            Channel channel = new Channel(music, AmDecoder.of(new MusicInstance(musicProvider, music)));
            channel.setVolume(volume);
            return channel;
        }
        catch (Exception ex) {
            throw new RuntimeException("Could not create audio channel for music-file '" + music.path() + "'", ex);
        }
    }


    public void pause(boolean doFade) {
        synchronized (stack) {
            if (!stack.empty())
                stack.peek().right().pause(doFade);
        }
    }

    public void resume(int priority, boolean doFade) {
        synchronized (stack) {
            stopAllAbove(priority, doFade);
            resume(doFade);
        }
    }

    public void resume(boolean doFade) {
        Channel channel;
        synchronized (stack) {
            if (!stack.empty())
                channel = stack.peek().right();
            else
                return;
        }
        innerResume(channel, doFade);
    }

    private void innerResume(Channel channel, boolean doFade) {
        notifyListeners(channel);
        channel.resume(doFade);
    }

    public void stopAll() {
        synchronized (stack) {
            for (var pair : stack)
                Utils.ignoreException(() -> pair.right().stopAndClose(false));
            stack.clear();
        }
    }

    public boolean isPlaying() {
        synchronized (stack) {
            return !stack.empty() && stack.peek().right().isPlaying();
        }
    }


    private void notifyListeners(Channel channel) {
        var nowPlaying = new NowPlaying(
                channel.music,
                channel.getMusicPath(),
                channel.getMusicTitle(),
                channel.getMusicAuthor()
        );
        for (var listener : nowPlayingListeners)
            listener.accept(nowPlaying);
    }

    public void addNowPlayingListener(Consumer<NowPlaying> listener) {
        nowPlayingListeners.add(listener);
    }

    public void removeNowPlayingListener(Consumer<NowPlaying> listener) {
        nowPlayingListeners.remove(listener);
    }


    public record NowPlaying(Music music, String path, @Nullable String title, String author) {
        public String titleOrPath() {
            return title == null ? path : title;
        }
    }
}
