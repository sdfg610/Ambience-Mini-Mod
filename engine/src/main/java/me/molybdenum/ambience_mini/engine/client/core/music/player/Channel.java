package me.molybdenum.ambience_mini.engine.client.core.music.player;

import me.molybdenum.ambience_mini.engine.client.core.music.AL.ALException;
import me.molybdenum.ambience_mini.engine.shared.core.music.Music;
import me.molybdenum.ambience_mini.engine.client.core.music.decoders.AmDecoder;
import me.molybdenum.ambience_mini.engine.client.core.music.AL.ALUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL10;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Channel implements PlayState {
    private static final int BUFFER_COUNT = 4;

    private static final long FADE_STEP_MILLISECONDS = 75;
    private static final int FADE_STEP_COUNT = 10;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();

    private float playingVolume;
    private final AtomicBoolean isFading = new AtomicBoolean();

    public final MusicInstance inst;
    public final Music music;
    private final AmDecoder decoder;
    private final int source;

    private boolean isInitialized = false;
    private boolean isClosed = false;
    private long processedSamples = 0;


    public Channel(MusicInstance inst) {
        this.inst = inst;
        this.music = inst.music;
        this.decoder = inst.createDecoder();
        this.source = ALUtils.genSource();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // State and info
    @Override
    public long getSamplePosition() {
        if (readLock.tryLock()) {
            try {
                if (!isClosed) {
                    long position = processedSamples + ALUtils.getBufferSamplePosition(source);

                    long start = getLoopStart();
                    return start > 0 && position >= getLoopStart()
                            ? ((position - start) % (getLoopEnd() - start)) + start
                            : position;
                }
            } finally {
                readLock.unlock();
            }
        }
        return 0;
    }

    @Override
    public String getPath() {
        return decoder.inst.music.path();
    }

    @Override
    public @Nullable String getTitleOrNull() {
        return decoder.info.getTitleOrNull();
    }

    @Override
    public @Nullable String getAuthorOrNull() {
        return decoder.info.getAuthorOrNull();
    }

    @Override
    public long getTotalSamples() {
        return decoder.info.getTotalSamples();
    }

    @Override
    public int getSamplesPerSecond() {
        return decoder.info.getSampleRate();
    }

    @Override
    public long getLoopStart() {
        return decoder.getLoopStart();
    }

    @Override
    public long getLoopEnd() {
        return decoder.getLoopEnd();
    }


    @Override
    public boolean isPlaying() {
        try {
            readLock.lock();
            return !isClosed && ALUtils.getSourceState(source) == AL10.AL_PLAYING;
        } finally {
            readLock.unlock();
        }
    }

    public boolean isPaused() {
        try {
            readLock.lock();
            if (!isClosed) {
                int state = ALUtils.getSourceState(source);
                return state == AL10.AL_PAUSED || state == AL10.AL_INITIAL;
            }
            return false;
        } finally {
            readLock.unlock();
        }
    }

    public boolean isStopped() {
        try {
            readLock.lock();
            return !isClosed && ALUtils.getSourceState(source) == AL10.AL_STOPPED;
        } finally {
            readLock.unlock();
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }



    // -----------------------------------------------------------------------------------------------------------------
    // Control
    public void setVolume(float volume) {
        playingVolume = volume + music.getCorrectedVolumeAdjustment();
        try {
            readLock.lock();
            if (!isClosed) {
                try {
                    ALUtils.setMaxVolume(source, playingVolume);
                    if (isPlaying() && !isFading.get())
                        ALUtils.setVolume(source, playingVolume);
                } catch (ALException ignored) { }
            }
        } finally {
            readLock.unlock();
        }
    }

    public void resume(boolean fadeIn) {
        try {
            readLock.lock();
            if (!isClosed && isPaused()) {
                ALUtils.setVolume(source, fadeIn ? 0f : playingVolume);
                ALUtils.resumeSource(source);
                if (fadeIn) fadeIn();
            }
        } finally {
            readLock.unlock();
        }
    }

    public void pause(boolean fadeOut) {
        try {
            readLock.lock();
            if (!isClosed && isPlaying()) {
                if (fadeOut) fadeOut();
                ALUtils.pauseSource(source);
            }
        } finally {
            readLock.unlock();
        }
    }

    public void stopAndClose(boolean fadeOut) {
        try {
            lock.writeLock().lock();
            if (!isClosed) {
                isClosed = true;
                if (fadeOut) fadeOut();
                ALUtils.stopSource(source);
                removeProcessedBuffers();
                ALUtils.deleteSource(source);
                decoder.close();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void fadeIn() {
        isFading.set(true);
        try {
            float diff = playingVolume / FADE_STEP_COUNT;

            for (int i = FADE_STEP_COUNT - 1; i >= 0; i--) {
                ALUtils.setVolume(source, playingVolume - diff*i);
                TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
            }

            ALUtils.setVolume(source, playingVolume);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            isFading.set(false);
        }
    }

    private void fadeOut() {
        isFading.set(true);
        try {
            float diff = playingVolume / FADE_STEP_COUNT;

            for (int i = 0; i < FADE_STEP_COUNT; i++) {
                ALUtils.setVolume(source, playingVolume - diff*i);
                TimeUnit.MILLISECONDS.sleep(FADE_STEP_MILLISECONDS);
            }

            ALUtils.setVolume(source, 0f);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        finally {
            isFading.set(false);
        }
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Buffering
    public void initBuffers() {
        try {
            readLock.lock();
            if (!isClosed) {
                loadNextBuffers(BUFFER_COUNT);
            }
        } finally {
            readLock.unlock();
        }
    }

    public void updateBuffers() {
        try {
            readLock.lock();
            if (!isClosed)
                loadNextBuffers(removeProcessedBuffers());
        } finally {
            readLock.unlock();
        }
    }

    private void loadNextBuffers(int count) {
        for (int i = 0; i < count; ++i) {
            ByteBuffer data = decoder.getFrame();
            if (data == null)
                return;

            int buffer = ALUtils.genBuffer();
            ALUtils.bufferData(buffer, data, decoder.getFormat());
            ALUtils.queueBuffer(source, buffer);
            isInitialized = true;
        }
    }

    private int removeProcessedBuffers() {
        int count = ALUtils.countProcessedBuffers(source);
        if (count > 0)
            processedSamples += ALUtils.deleteBuffers(ALUtils.unqueueBuffers(source, count)) / decoder.info.getBytesInSample();
        return count;
    }
}
