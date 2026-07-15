package me.molybdenum.ambience_mini.engine.client.music;

import me.molybdenum.ambience_mini.engine.client.configuration.Music;
import me.molybdenum.ambience_mini.engine.client.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.client.music.decoders.AmDecoder;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Consumer;

public class MusicPlayer {
    private final Stack<Pair<Integer, Channel>> stack = new Stack<>(); // Priorities and channels

    private final MusicProvider musicProvider;
    private final Runnable onRanToEnd;

    private float currentMusicVolume;

    private final List<Consumer<NowPlaying>> nowPlayingListeners = new ArrayList<>();


    public MusicPlayer(MusicProvider musicProvider, Runnable onRanToEnd) {
        this.musicProvider = musicProvider;
        this.onRanToEnd = onRanToEnd;
    }


    public void updateBuffers() {
        synchronized (stack) {
            for (var pair : stack) {
                var channel = pair.right();
                channel.updateBuffers();
                if (channel.isStopped()) {
                    channel.stopAndClose(false);
                    onRanToEnd.run();
                }
            }
        }
    }

    public void setVolume(Float volume) {
        currentMusicVolume = volume;
        synchronized (stack) {
            stack.forEach(pair -> pair.right().setVolume(volume));
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

        synchronized (stack) {
            try {
                stopAllAbove(priority-1, doFade);
                Channel channel = createChannel(music, currentMusicVolume);
                stack.push(new Pair<>(priority, channel));
                innerResume(channel, doFade);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File '" + music.path() +  "' not found. Fix your Ambience config!", e);
            }
        }
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
        synchronized (stack) {
            if (!stack.empty())
                innerResume(stack.peek().right(), doFade);
        }
    }

    private void innerResume(Channel channel, boolean doFade) {
        notifyListeners(channel);
        channel.resume(doFade);
    }

    public void stopAll() {
        synchronized (stack) {
            stack.forEach(pair -> pair.right().stopAndClose(false));
            stack.clear();
        }
    }

    public boolean isPlaying() {
        synchronized (stack) {
            return !stack.empty() && stack.peek().right().isPlaying();
        }
    }


    private void notifyListeners(Channel channel) {
        var title = channel.getMusicTitle();
        var titleOrPath = title == null ? channel.getMusicPath() : title;
        var author = channel.getMusicAuthor();
        var nowPlaying = new NowPlaying(titleOrPath, author);

        for (var listener : nowPlayingListeners)
            listener.accept(nowPlaying);
    }

    public void addListener(Consumer<NowPlaying> listener) {
        nowPlayingListeners.add(listener);
    }

    public void removeListener(Consumer<NowPlaying> listener) {
        nowPlayingListeners.remove(listener);
    }


    public record NowPlaying(String titleOrPath, String author) { }
}
