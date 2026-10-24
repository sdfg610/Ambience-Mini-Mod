package me.molybdenum.ambience_mini.engine.client.core.music.player;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.misc.BaseNotification;
import me.molybdenum.ambience_mini.engine.client.core.music.ServerMusicCache;
import me.molybdenum.ambience_mini.engine.shared.jobs.JobCenter;
import me.molybdenum.ambience_mini.engine.shared.core.music.Music;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

public class MusicPlayer
{
    private static final int BUFFER_UPDATE_INTERVAL_MS = 500;

    private final Stack<ChannelEntry> stack = new Stack<>(); // Priorities and channels

    private final BaseNotification<?> notification;
    private final ServerMusicCache musicCache;
    private final Function<Music, MusicInstance> instantiate;
    private final Runnable onRanToEnd;
    private final Consumer<Exception> onBufferException;

    private float currentMusicVolume;

    private final List<Consumer<PlayState>> nowPlayingListeners = new ArrayList<>();

    private final JobCenter jobCenter = JobCenter.variable(3);
    private MusicBufferJob bufferJob;

    private Channel currentChannel;


    @SuppressWarnings("rawtypes")
    public MusicPlayer(
            BaseClientCore core,
            Runnable onRanToEnd,
            Consumer<Exception> onBufferException
    ) {
        this.notification = core.notification;
        this.musicCache = core.musicCache;
        this.instantiate = core::instantiate;
        this.onRanToEnd = onRanToEnd;
        this.onBufferException = onBufferException;
    }


    public void shutdown() {
        jobCenter.shutdown();
        synchronized (stack) {
            for (var entry : stack)
                Utils.ignoreException(() -> entry.ifPresent(ch -> ch.stopAndClose(false)));
            stack.clear();
        }
    }


    // -----------------------------------------------------------------------------------------------------------------
    // State
    public PlayState getCurrentPlayState() {
        return currentChannel;
    }

    public boolean isPlaying() {
        synchronized (stack) {
            return !stack.empty() && stack.peek().ifPresent(Channel::isPlaying, false);
        }
    }

    public boolean isSilence() {
        synchronized (stack) {
            return !stack.empty() && stack.peek().channel == null;
        }
    }

    public boolean isInitialized(int priority) {
        synchronized (stack) {
            for (var entry : stack)
                if (entry.priority == priority) {
                    var ch = entry.channel;
                    return ch == null || ch.isInitialized();
                }
            return false;
        }
    }

    public List<String> getActiveServerSoundtracks() {
        synchronized (stack) {
            return stack.stream()
                    .filter(entry -> entry.ifPresent(ch -> ch.music.locatedOnServer(), false))
                    .map(entry -> {
                        assert entry.channel != null;
                        return entry.channel.getPath();
                    })
                    .toList();
        }
    }

    @Nullable
    public Music getMusicByPriority(int priority) {
        synchronized (stack) {
            for (var entry : stack)
                if (entry.priority == priority)
                    return entry.ifPresent(ch -> ch.music, null);
        }
        return null;
    }

    public int getTopPriority() {
        synchronized (stack) {
            return stack.empty() ? -1 : stack.peek().priority;
        }
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Control
    public void setVolume(float volume) {
        if (Math.abs(currentMusicVolume - volume) >= 0.01f) {
            currentMusicVolume = volume;
            for (var entry : stack)
                entry.ifPresent(ch -> ch.setVolume(volume));
        }
    }

    public void silence(int priority, boolean doFade) {
        if (priority < 0)
            throw new RuntimeException("Priority of music cannot be negative!");

        synchronized (stack) {
            stopGreaterThanEqual(priority, doFade);
            pause(doFade);
            stack.push(new ChannelEntry(null, priority));
        }
    }

    public void play(int priority, Music music, boolean doFade) {
        if (priority < 0)
            throw new RuntimeException("Priority of music cannot be negative!");

        Channel channel = createChannel(music, currentMusicVolume);
        synchronized (stack) {
            stopGreaterThanEqual(priority, doFade);
            pause(doFade);
            stack.push(new ChannelEntry(channel, priority));
            jobCenter.post(new InitMusicBufferJob(channel, doFade));
        }
    }

    public void resume(int priority, boolean fadeOut, boolean fadeIn) {
        if (priority < 0)
            throw new RuntimeException("Priority of music cannot be negative!");

        synchronized (stack) {
            stopGreaterThanEqual(priority+1, fadeOut);
            if (!stack.empty()) {
                var entry = stack.peek();
                if (entry.priority == priority) {
                    entry.ifPresent(ch -> bufferAndResumeAndNotify(ch, fadeIn));
                    return;
                }
            }
        }
        throw new RuntimeException("No channel with priority '" + priority + "'");
    }

    public void pause(boolean doFade) {
        synchronized (stack) {
            if (!stack.empty())
                stack.peek().ifPresent(ch -> ch.pause(doFade));
        }
    }

    public void stopGreaterThanEqual(int priority, boolean fadeOut) {
        synchronized (stack) {
            while (!stack.empty() && stack.peek().priority >= priority)
                stack.pop().ifPresent(ch -> ch.stopAndClose(fadeOut));
        }
    }


    private Channel createChannel(Music music, float volume) {
        try {
            Channel channel = new Channel(instantiate.apply(music));
            channel.setVolume(volume);
            return channel;
        } catch (Exception ex) {
            throw new RuntimeException("Could not create audio channel for music-file '" + music.path() + "'", ex);
        }
    }

    private void bufferAndResumeAndNotify(Channel channel, boolean doFade) {
        try {
            var res = musicCache.bufferThis(channel.music);
            if (res.isFailure())
                notification.printToChat(res.error);
        } catch (FileNotFoundException ignored) { }

        if (bufferJob != null)
            bufferJob.cancel(false);
        bufferJob = jobCenter.schedule(new MusicBufferJob(channel), 0, BUFFER_UPDATE_INTERVAL_MS);

        currentChannel = channel;
        channel.resume(doFade);
        notifyListeners(channel);
    }


    public void addNowPlayingListener(Consumer<PlayState> listener) {
        nowPlayingListeners.add(listener);
    }

    public void removeNowPlayingListener(Consumer<PlayState> listener) {
        nowPlayingListeners.remove(listener);
    }

    private void notifyListeners(Channel channel) {
        for (var listener : nowPlayingListeners)
            listener.accept(channel);
    }


    private record ChannelEntry(@Nullable Channel channel, int priority) {
        public void ifPresent(Consumer<Channel> body) {
            if (channel != null)
                body.accept(channel);
        }

        public <T> T ifPresent(Function<Channel, T> body, T defaultValue) {
            return channel != null ? body.apply(channel) : defaultValue;
        }
    }

    private class InitMusicBufferJob extends JobCenter.Job
    {
        private final Channel channel;
        private final boolean doFade;


        public InitMusicBufferJob(Channel channel, boolean doFade) {
            this.channel = channel;
            this.doFade = doFade;
        }

        @Override
        protected void body() {
            channel.initBuffers();
            synchronized (stack) {
                if (!stack.empty() && stack.peek().channel == channel) // Only if this channel is still top priority...
                    bufferAndResumeAndNotify(channel, doFade);
            }
        }

        @Override
        protected void onException(Exception ex) {
            onBufferException.accept(ex);
        }
    }

    private class MusicBufferJob extends JobCenter.Job
    {
        private final Channel channel;


        public MusicBufferJob(Channel channel) {
            this.channel = channel;
        }

        @Override
        protected void body() {
            channel.updateBuffers();
            if (channel.isStopped()) {
                channel.stopAndClose(false);
                onRanToEnd.run();
            }
        }

        @Override
        protected void onException(Exception ex) {
            onBufferException.accept(ex);
        }
    }
}
