package me.molybdenum.ambience_mini.engine.client.music;

import org.lwjgl.openal.AL10;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;


public class ALUtils
{
    // -----------------------------------------------------------------------------------------------------------------
    // Source control
    public static int genSource() {
        int[] source = new int[1];
        AL10.alGenSources(source);
        throwIfAlError();
        return source[0];
    }

    public static void deleteSource(int source) {
        AL10.alDeleteSources(new int[] { source });
        throwIfAlError();
    }


    public static void queueBuffer(int source, int buffer) {
        AL10.alSourceQueueBuffers(source, new int[]{ buffer });
        throwIfAlError();
    }

    public static int countProcessedBuffers(int source) {
        int count = AL10.alGetSourcei(source, AL_BUFFERS_PROCESSED);
        throwIfAlError();
        return count;
    }

    public static int[] unqueueBuffers(int source, int count) {
        int[] buffers = new int[count];
        AL10.alSourceUnqueueBuffers(source, buffers);
        throwIfAlError();
        return buffers;
    }


    public static int getSourceState(int source) {
        int state = AL10.alGetSourcei(source, AL_SOURCE_STATE);
        throwIfAlError();
        return state;
    }

    public static void resumeSource(int source) {
        AL10.alSourcePlay(source);
        throwIfAlError();
    }

    public static void pauseSource(int source) {
        AL10.alSourcePause(source);
        throwIfAlError();
    }

    public static void stopSource(int source) {
        AL10.alSourceStop(source);
        throwIfAlError();
    }


    public static void setVolume(int source, float volume) {
        AL10.alSourcef(source, AL_GAIN, volume);
        throwIfAlError();
    }

    public static void setMaxVolume(int source, float maxVolume) {
        AL10.alSourcef(source, AL_MAX_GAIN, maxVolume);
        throwIfAlError();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Buffer control
    public static int genBuffer() {
        int[] source = new int[1];
        AL10.alGenBuffers(source);
        throwIfAlError();
        return source[0];
    }

    public static void bufferData(int buffer, ByteBuffer data, AudioFormat format) {
        alBufferData(buffer, audioFormatToOpenAl(format), data, (int)format.getSampleRate());
        throwIfAlError();
    }

    public static void deleteBuffers(int[] buffers) {
        AL10.alDeleteBuffers(buffers);
        throwIfAlError();
    }


    public static void throwIfAlError() {
        int err = AL10.alGetError();
        if (err != AL_NO_ERROR) {
            throw new RuntimeException(alGetString(err));
        }
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Helpers
    public static int audioFormatToOpenAl(AudioFormat format) {
        AudioFormat.Encoding encoding = format.getEncoding();
        int channels = format.getChannels();
        int sampleSize = format.getSampleSizeInBits();
        if (encoding.equals(AudioFormat.Encoding.PCM_UNSIGNED) || encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
            if (channels == 1) {
                if (sampleSize == 8) {
                    return AL_FORMAT_MONO8;
                }

                if (sampleSize == 16) {
                    return AL_FORMAT_MONO16;
                }
            } else if (channels == 2) {
                if (sampleSize == 8) {
                    return AL_FORMAT_STEREO8;
                }

                if (sampleSize == 16) {
                    return AL_FORMAT_STEREO16;
                }
            }
        }

        throw new IllegalArgumentException("Invalid audio format: " + format);
    }
}
