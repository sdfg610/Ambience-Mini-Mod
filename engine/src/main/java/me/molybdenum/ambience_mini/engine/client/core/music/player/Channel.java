package me.molybdenum.ambience_mini.engine.client.core.music.player;

import me.molybdenum.ambience_mini.engine.client.core.music.AL.ALException;
import me.molybdenum.ambience_mini.engine.shared.core.music.Music;
import me.molybdenum.ambience_mini.engine.client.core.music.decoders.AmDecoder;
import me.molybdenum.ambience_mini.engine.client.core.music.AL.ALUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL10;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Channel implements PlayState {
    private static final int BUFFER_COUNT = 4;

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

    public void resume(int fadeTimeMillis) {
        try {
            readLock.lock();
            if (!isClosed && isPaused()) {
                ALUtils.setVolume(source, fadeTimeMillis > 0 ? 0f : playingVolume);
                ALUtils.resumeSource(source);
                fadeIn(fadeTimeMillis);
            }
        } finally {
            readLock.unlock();
        }
    }

    public void pause(int fadeTimeMillis) {
        try {
            readLock.lock();
            if (!isClosed && isPlaying()) {
                fadeOut(fadeTimeMillis);
                ALUtils.pauseSource(source);
            }
        } finally {
            readLock.unlock();
        }
    }

    public void stopAndClose(int fadeTimeMillis) {
        try {
            lock.writeLock().lock();
            if (!isClosed) {
                isClosed = true;
                fadeOut(fadeTimeMillis);
                ALUtils.stopSource(source);
                removeProcessedBuffers();
                ALUtils.deleteSource(source);
                decoder.close();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void fadeIn(int fadeTimeMillis) {
        if (fadeTimeMillis > 0) {
            isFading.set(true);

            try {
                long now = System.currentTimeMillis();
                long elapsed;
                while ((elapsed = System.currentTimeMillis() - now) < fadeTimeMillis) {
                    ALUtils.setVolume(source, playingVolume * ((float) elapsed / fadeTimeMillis));
                    //noinspection BusyWait
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        ALUtils.setVolume(source, playingVolume);
        isFading.set(false);
    }

    private void fadeOut(int fadeTimeMillis) {
        if (fadeTimeMillis > 0) {
            isFading.set(true);

            try {
                long now = System.currentTimeMillis();
                long elapsed;
                while ((elapsed = System.currentTimeMillis() - now) < fadeTimeMillis) {
                    ALUtils.setVolume(source, playingVolume * (1f - (float) elapsed / fadeTimeMillis));
                    //noinspection BusyWait
                    Thread.sleep(20);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        ALUtils.setVolume(source, 0f);
        isFading.set(false);
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
