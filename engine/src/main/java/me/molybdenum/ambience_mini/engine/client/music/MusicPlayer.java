package me.molybdenum.ambience_mini.engine.client.music;

import me.molybdenum.ambience_mini.engine.client.configuration.Music;
import me.molybdenum.ambience_mini.engine.client.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.client.music.decoders.AmDecoder;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Optional;
import java.util.Stack;

public class MusicPlayer {
    private final Stack<Pair<Integer, Channel>> stack = new Stack<>(); // Priorities and channels

    private final MusicProvider musicProvider;
    private final Runnable onRanToEnd;

    private float currentMusicVolume;


    public MusicPlayer(MusicProvider musicProvider, Runnable onRanToEnd) {
        this.musicProvider = musicProvider;
        this.onRanToEnd = onRanToEnd;
    }


    public void updateBuffers() {
        synchronized (stack) {
            if (!stack.empty()) {
                var channel = stack.peek().right();
                channel.updateBuffers(); // Only top channel can play and needs update.
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
                Channel channel = createChannel(music);
                stack.push(new Pair<>(priority, channel));
                channel.setVolume(currentMusicVolume);
                channel.resume(doFade);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File '" + music.musicPath() +  "' not found. Fix your Ambience config!", e);
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

    private Channel createChannel(Music music) throws FileNotFoundException {
        try {
            return new Channel(
                    music,
                    AmDecoder.of(music.getExtension(), musicProvider.getMusicStream(music.musicPath()))
            );
        }
        catch (Exception ex) {
            throw new RuntimeException("Could not create audio channel for music-file '" + music.musicPath() + "'", ex);
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
                stack.peek().right().resume(doFade);
        }
    }


    public boolean isPlaying() {
        synchronized (stack) {
            return !stack.empty() && stack.peek().right().isPlaying();
        }
    }


    public void stopAll() {
        synchronized (stack) {
            stack.forEach(pair -> pair.right().stopAndClose(false));
            stack.clear();
        }
    }
}
