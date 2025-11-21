package me.molybdenum.ambience_mini.engine.music.players;

/* libFLAC - Free Lossless Audio Codec library
 * Copyright (C) 2000,2001,2002,2003  Josh Coalson
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

import java.io.EOFException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.sound.sampled.*;

import org.jetbrains.annotations.NotNull;
import org.jflac.FLACDecoder;
import org.jflac.frame.Frame;
import org.jflac.metadata.Metadata;
import org.jflac.metadata.StreamInfo;
import org.jflac.util.ByteData;


/**
 * Play a FLAC file application.
 * @author kc7bfi
 */
public class FlacPlayer implements Player {
    private SourceDataLine line;

    private final AtomicBoolean doStop = new AtomicBoolean(false);
    private final AtomicBoolean doPlay = new AtomicBoolean(false);

    private final InputStream stream;
    private final String musicName;
    private final Consumer<Exception> onException;
    private final Consumer<Boolean> onFinishedPlaying; // Boolean indicating whether the music was stopped (true) or ran to end (false)

    private float defaultGain;


    public FlacPlayer(
            InputStream stream,
            String musicName,
            Consumer<Exception> onException,
            Consumer<Boolean> onFinishedPlaying
    ) {
        this.stream = stream;
        this.musicName = musicName;
        this.onException = onException;
        this.onFinishedPlaying = onFinishedPlaying;

        getPlayerThread().start();
    }


    // ------------------------------------------------------------------------------------------
    // Controls
    public boolean isPlaying() {
        return doPlay.get();
    }

    public void play() {
        doPlay.set(true);
    }

    public void pause() {
        doPlay.set(false);
    }

    public void stop() {
        doPlay.set(false);
        doStop.set(true);
    }

    public void setGain(float gain) {
        if (line==null)
            defaultGain = gain;
        else
            ((FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN)).setValue(gain);
    }


    // ------------------------------------------------------------------------------------------
    // Music logic
    private @NotNull Thread getPlayerThread() {
        Thread _playerThread = new Thread(() -> {
            try {
                FLACDecoder decoder = new FLACDecoder(stream);

                Metadata md = Arrays.stream(decoder.readMetadata())
                        .filter(m -> m instanceof StreamInfo)
                        .findFirst()
                        .orElse(null);
                if (md instanceof StreamInfo streamInfo)
                    initializeSoundSource(streamInfo);
                else
                    throw new RuntimeException("There is no stream-info at the beginning of '" + musicName + "'. Cannot initialize sound-source!");

                try {
                    Frame frame = decoder.readNextFrame();
                    while (frame != null && !doStop.get()) {
                        while (!doPlay.get() && !doStop.get())
                            Thread.onSpinWait(); // Wait while paused and not stopped
                        ByteData bd = decoder.decodeFrame(frame, null);
                        line.write(bd.getData(), 0, bd.getLen());
                        frame = decoder.readNextFrame();
                    }
                    line.drain();
                } catch (EOFException ignored) { }
            }
            catch (Exception ex) {
                onException.accept(ex);
            }
            finally {
                try { line.close(); }
                catch (Exception ignored) { }

                doPlay.set(false);
                onFinishedPlaying.accept(doStop.get());
            }
        });
        _playerThread.setDaemon(true);
        _playerThread.setName("Ambience Mini - FLAC player Thread");
        return _playerThread;
    }

    public void initializeSoundSource(StreamInfo streamInfo) {
        try {
            AudioFormat fmt = streamInfo.getAudioFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt, AudioSystem.NOT_SPECIFIED);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(fmt, AudioSystem.NOT_SPECIFIED);
            ((FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN)).setValue(defaultGain);
            line.start();
        } catch (Exception e) {
            onException.accept(e);
        }
    }
}