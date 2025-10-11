/*
 * 11/19/04		1.0 moved to LGPL. 
 *-----------------------------------------------------------------------
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package javazoom.jlayer.player;

import javazoom.jlayer.decoder.*;
import javazoom.jlayer.player.audio.JavaSoundAudioDevice;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * a hybrid of javazoom.jl.player.Player tweeked to include <code>play(startFrame, endFrame)</code>
 * hopefully this will be included in the api
 */
public class NewMP3Player
{
	private final Decoder decoder = new Decoder();
	private final JavaSoundAudioDevice audioDevice = new JavaSoundAudioDevice();

	private final AtomicBoolean doStop = new AtomicBoolean(false);
	private final AtomicBoolean doPlay = new AtomicBoolean(false);

	private final Bitstream bitstream;
	private final float initialGain;

	private final Consumer<Exception> onException;
	private final Consumer<Boolean> onFinishedPlaying; // Boolean indicating whether the music was stopped (true) or ran to end (false)


	public NewMP3Player(
			InputStream stream,
			float initialGain,
			Consumer<Exception> onException,
			Consumer<Boolean> onFinishedPlaying
	) {
		this.bitstream = new Bitstream(stream);
		this.initialGain = initialGain;
		this.onException = onException;
		this.onFinishedPlaying = onFinishedPlaying;

		getPlayerThread().start();
	}


	// ------------------------------------------------------------------------------------------
	// Controls
	public boolean isPlaying() {
		return doPlay.get();
	}

	public void resume() {
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
		try {
			audioDevice.setGain(gain);
		} catch (Throwable ignored)
		{ } // If you can't fix the bug just put a catch around it
	}


	// ------------------------------------------------------------------------------------------
	// Music logic
	private @NotNull Thread getPlayerThread() {
		Thread _playerThread = new Thread(() -> {
			try {
				audioDevice.open(decoder);
				audioDevice.setGain(initialGain);

				boolean keepGoing = true;
				while (keepGoing && !doStop.get()) {
					while (!doPlay.get() && !doStop.get())
						Thread.onSpinWait(); // Wait while paused and not stopped
					keepGoing = decodeFrame();
				}
				audioDevice.flush();

				onFinishedPlaying.accept(doStop.get());
			}
			catch (Exception ex) {
				onException.accept(ex);
			}
			finally {
				audioDevice.close();
				try { bitstream.close(); }
				catch (BitstreamException ignored) { }

				doPlay.set(false);
			}
		});
		_playerThread.setDaemon(true);
		_playerThread.setName("Ambience Mini - MP3 player Thread");
		return _playerThread;
	}

	/**
	 * Decodes a single frame.
	 * @return true if there are more frames to decode, false otherwise.
	 */
	private boolean decodeFrame() throws JavaLayerException
	{
		Header h = bitstream.readFrame();
		if (h == null)
			return false;

		SampleBuffer output = (SampleBuffer)decoder.decodeFrame(h, bitstream);
		audioDevice.write(output.getBuffer(), 0, output.getBufferLength());

		bitstream.closeFrame();
        return true;
	}
}