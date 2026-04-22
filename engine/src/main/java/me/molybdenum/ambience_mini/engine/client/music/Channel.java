package me.molybdenum.ambience_mini.engine.client.music;

import me.molybdenum.ambience_mini.engine.client.configuration.Music;
import me.molybdenum.ambience_mini.engine.client.music.decoders.AmDecoder;
import org.lwjgl.openal.AL10;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class Channel {
    public static final int BUFFER_COUNT = 4;

    private static final long FADE_STEP_MILLISECONDS = 75;
    private static final int FADE_STEP_COUNT = 10;

    private float playingVolume;

    public final Music music;
    private final int source;
    private final AmDecoder decoder;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);


    public Channel(Music music, AmDecoder decoder) {
        this.music = music;
        this.decoder = decoder;

        this.source = ALUtils.genSource();
        loadNextBuffers(BUFFER_COUNT);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Stream management
    public void updateBuffers() {
        if (!isClosed.get()) {
            int count = removeProcessedBuffers();
            loadNextBuffers(count);
        }
    }

    private void loadNextBuffers(int count) {
        for (int i = 0; i < count; ++i) {
            ByteBuffer data = decoder.getFrame();
            if (data != null) {
                int buffer = ALUtils.genBuffer();
                ALUtils.bufferData(buffer, data, decoder.getFormat());
                ALUtils.queueBuffer(source, buffer);
            }
        }
    }

    private int removeProcessedBuffers() {
        int count = ALUtils.countProcessedBuffers(source);
        if (count > 0)
            ALUtils.deleteBuffers(ALUtils.unqueueBuffers(source, count));
        return count;
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Sound control
    public void resume(boolean fadeIn) {
        if (!isClosed.get() && isPaused()) {
            ALUtils.setVolume(source, fadeIn ? 0f : playingVolume);
            ALUtils.resumeSource(source);
            if (fadeIn) fadeIn();
        }
    }

    public void pause(boolean fadeOut) {
        if (!isClosed.get() && isPlaying()) {
            if (fadeOut) fadeOut();
            ALUtils.pauseSource(source);
        }
    }


    public void setVolume(float volume) {
        if (!isClosed.get()) {
            playingVolume = volume + music.getFractionalAdjustment();
            ALUtils.setMaxVolume(source, playingVolume);
            if (isPlaying())
                ALUtils.setVolume(source, playingVolume);
        }
    }

    public void stopAndClose(boolean fadeOut) {
        if (this.isClosed.compareAndSet(false, true)) {
            if (fadeOut) fadeOut();
            ALUtils.stopSource(source);
            removeProcessedBuffers();
            ALUtils.deleteSource(source);
            decoder.close();
        }
    }

    private void fadeIn() {
        float diff = playingVolume / FADE_STEP_COUNT;
        try {
            for (int i = FADE_STEP_COUNT - 1; i >= 0; i--) {
                ALUtils.setVolume(source, playingVolume - diff*i);
                TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
            }
        } catch (Throwable ignored) { }

        ALUtils.setVolume(source, playingVolume);
    }

    private void fadeOut() {
        float diff = playingVolume / FADE_STEP_COUNT;
        try {
            for (int i = 0; i < FADE_STEP_COUNT; i++) {
                ALUtils.setVolume(source, playingVolume - diff*i);
                TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
            }
        } catch (Throwable ignored) { }

        ALUtils.setVolume(source, 0f);
    }


    public boolean isPlaying() {
        return !isClosed.get() && ALUtils.getSourceState(source) == AL10.AL_PLAYING;
    }

    public boolean isPaused() {
        int state = ALUtils.getSourceState(source);
        return !isClosed.get() && state == AL10.AL_PAUSED || state == AL10.AL_INITIAL;
    }

    public boolean isStopped() {
        return !isClosed.get() && ALUtils.getSourceState(source) == AL10.AL_STOPPED;
    }

    public boolean isClosed() {
        return isClosed.get();
    }
}
