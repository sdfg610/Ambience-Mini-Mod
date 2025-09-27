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

package javazoom.jlayer.player.advanced;

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javazoom.jlayer.player.AudioDevice;
import javazoom.jlayer.player.FactoryRegistry;
import javazoom.jlayer.player.JavaSoundAudioDevice;
import javazoom.jlayer.decoder.Bitstream;
import javazoom.jlayer.decoder.BitstreamException;
import javazoom.jlayer.decoder.Decoder;
import javazoom.jlayer.decoder.Header;
import javazoom.jlayer.decoder.JavaLayerException;
import javazoom.jlayer.decoder.SampleBuffer;

/**
 * a hybrid of javazoom.jl.player.Player tweeked to include <code>play(startFrame, endFrame)</code>
 * hopefully this will be included in the api
 */
public class AdvancedPlayer
{
	/** The MPEG audio bitstream.*/
	private Bitstream bitstream;

	/** The MPEG audio decoder. */
	private Decoder decoder;

	/** The AudioDevice the audio samples are written to. */
	private AudioDevice audioDevice;

	/** Has the player been closed? */
	private boolean closed = false;

	/** Has the player played back all frames from the stream? */
	private int lastPosition = 0;

	/** Listener for the playback process */
	private PlaybackListener listener;


	/**
	 * Creates a new <code>Player</code> instance.
	 */
	public AdvancedPlayer(InputStream stream) throws JavaLayerException
	{
		this(stream, null, 0f);
	}

	/**
	 * Creates a new <code>Player</code> instance.
	 */
	public AdvancedPlayer(InputStream stream, float gain) throws JavaLayerException
	{
		this(stream, null, gain);
	}

	public AdvancedPlayer(InputStream stream, AudioDevice device, float gain) throws JavaLayerException
	{
		bitstream = new Bitstream(stream);

		if (device != null)
			audioDevice = device;
		else
			audioDevice = FactoryRegistry.systemRegistry().createAudioDevice();
		audioDevice.open(decoder = new Decoder());
		((JavaSoundAudioDevice) audioDevice).setDefaultGain(gain);
	}

	public void play(AtomicBoolean isPlaying) throws JavaLayerException
	{
		play(Integer.MAX_VALUE, isPlaying);
	}

	/**
	 * Plays a number of MPEG audio frames.
	 *
	 * @param frames	The number of frames to play.
	 * @return	true if the last frame was played, or false if there are
	 *			more frames.
	 */
	public boolean play(int frames, AtomicBoolean isPlaying) throws JavaLayerException
	{
		boolean ret = true;

		if(listener != null)
			listener.playbackStarted(createEvent(PlaybackEvent.STARTED));

		while (frames-- > 0 && ret) {
			while (!isPlaying.get() && !closed)
				Thread.onSpinWait();
			ret = decodeFrame();
		}

		// last frame, ensure all data flushed to the audio device.
		AudioDevice out = audioDevice;
		if (out != null) {
			out.flush();
			synchronized (this) {
				close(); // Nulls audioDevice. Thus, we have "out".
			}

			if(listener != null)
				listener.playbackFinished(createEvent(out, PlaybackEvent.STOPPED));
		}

		return ret;
	}

	/**
	 * Closes this player. Any audio currently playing is stopped immediately.
	 */
	public synchronized void close()
	{
		AudioDevice out = audioDevice;
		closed = true;
		audioDevice = null;

		if (out != null) {
			// this may fail, so ensure object state is set up before calling this method.
			out.close();
			lastPosition = out.getPosition();

			try { bitstream.close(); }
			catch (BitstreamException ignored) { }
		}
	}

	/**
	 * Decodes a single frame.
	 *
	 * @return true if there are no more frames to decode, false otherwise.
	 */
	protected boolean decodeFrame() throws JavaLayerException
	{
		try {
			AudioDevice out = audioDevice;
			if (out == null) return false;

			Header h = bitstream.readFrame();
			if (h == null) return false;

			// sample buffer set when decoder constructed
			SampleBuffer output = (SampleBuffer) decoder.decodeFrame(h, bitstream);

			synchronized (this) {
				out.write(output.getBuffer(), 0, output.getBufferLength());
			}

			bitstream.closeFrame();
		}
		catch (RuntimeException ex) {
			throw new JavaLayerException("Exception decoding audio frame", ex);
		}
		return true;
	}

	/**
	 * skips over a single frame
	 * @return false	if there are no more frames to decode, true otherwise.
	 */
	protected boolean skipFrame() throws JavaLayerException {
		Header h = bitstream.readFrame();
		if (h == null) return false;
		bitstream.closeFrame();
		return true;
	}

	/**
	 * Plays a range of MPEG audio frames
	 * @param start	The first frame to play
	 * @param end		The last frame to play
	 * @return true if the last frame was played, or false if there are more frames.
	 */
	public boolean play(final int start, final int end) throws JavaLayerException
	{
		boolean ret = true;
		int offset = start;
		while (offset-- > 0 && ret) ret = skipFrame();
		return play(end - start, new AtomicBoolean());
	}

	/**
	 * Constructs a <code>PlaybackEvent</code>
	 */
	private PlaybackEvent createEvent(int id)
	{
		return createEvent(audioDevice, id);
	}

	/**
	 * Constructs a <code>PlaybackEvent</code>
	 */
	private PlaybackEvent createEvent(AudioDevice dev, int id)
	{
		return new PlaybackEvent(this, id, dev.getPosition());
	}

	/**
	 * sets the <code>PlaybackListener</code>
	 */
	public void setPlayBackListener(PlaybackListener listener)
	{
		this.listener = listener;
	}

	/**
	 * gets the <code>PlaybackListener</code>
	 */
	public PlaybackListener getPlayBackListener()
	{
		return listener;
	}

	/**
	 * closes the player and notifies <code>PlaybackListener</code>
	 */
	public void stop()
	{
		listener.playbackFinished(createEvent(PlaybackEvent.STOPPED));
		close();
	}


	/* ====================================================================================
	 * XXX
	 * Functions added by necessity, not present in the original code.
	 * ~GSTO
	 * ====================================================================================
	 */

	public void setGain(float gain) {
		try {
			((JavaSoundAudioDevice) audioDevice).setGain(gain);
		} catch (Throwable ignored)
		{ } // If you can't fix the bug just put a catch around it
	}
}