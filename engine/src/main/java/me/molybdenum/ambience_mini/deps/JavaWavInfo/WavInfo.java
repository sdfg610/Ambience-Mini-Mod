package me.molybdenum.ambience_mini.deps.JavaWavInfo;

// WavInfo by Rickey Ward
// based on wavIO by Evan X. Merz -> www.thisisnotalabel.com ("wav IO based on code by Evan Merz")
// do with this whatever you want provided you credit us appropriately ("WavInfo based on code by Even Merz and Rickey Ward")

import me.molybdenum.ambience_mini.deps.jid3lib.AbstractMP3Tag;
import me.molybdenum.ambience_mini.deps.jid3lib.MP3File;
import me.molybdenum.ambience_mini.deps.jid3lib.TagException;
import me.molybdenum.ambience_mini.deps.jid3lib.accessors.RandomArrayAccessor;
import me.molybdenum.ambience_mini.deps.jid3lib.id3.AbstractID3v2;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.*;

public class WavInfo {
	/*
	  WAV File Specification
	  FROM http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
	  EXTEND by RW fom http://soundfile.sapp.org/doc/WaveFormat/
	 The canonical WAVE format starts with the RIFF header:
	 0         4   ChunkID          Contains the letters "RIFF" in ASCII form
	                                (0x52494646 big-endian form).
	 4         4   ChunkSize        36 + SubChunk2Size, or more precisely:
	                                4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
	                                This is the size of the rest of the chunk 
	                                following this number.  This is the size of the 
	                                entire file in bytes minus 8 bytes for the
	                                two fields not included in this count:
	                                ChunkID and ChunkSize.
	 8         4   Format           Contains the letters "WAVE"
	                                (0x57415645 big-endian form).

	 The "WAVE" format consists of two subchunks: "fmt " and "data":
	 The "fmt " subchunk describes the sound data's format:
	 12        4   Subchunk1ID      Contains the letters "fmt "
	                                (0x666d7420 big-endian form).
	 16        4   Subchunk1Size    16 for PCM.  This is the size of the
	                                rest of the Subchunk which follows this number.
	 20        2   AudioFormat      PCM = 1 (i.e. Linear quantization)
	                                Values other than 1 indicate some 
	                                form of compression.
	 22        2   NumChannels      Mono = 1, Stereo = 2, etc.
	 24        4   SampleRate       8000, 44100, etc.
	 28        4   ByteRate         == SampleRate * NumChannels * BitsPerSample/8
	 32        2   BlockAlign       == NumChannels * BitsPerSample/8
	                                The number of bytes for one sample including
	                                all channels. I wonder what happens when
	                                this number isn't an integer?
	 34        2   BitsPerSample    8 bits = 8, 16 bits = 16, etc.
	 --        2   ExtraParamSize   if PCM, then doesn't exist
	 --        X   ExtraParams      space for extra parameters

	 The "data" subchunk contains the size of the data and the actual sound:
	 36        4   Subchunk2ID      Contains the letters "data"
	                                (0x64617461 big-endian form).
	 40        4   Subchunk2Size    == NumSamples * NumChannels * BitsPerSample/8
	                                This is the number of bytes in the data.
	                                You can also think of this as the size
	                                of the read of the subchunk following this 
	                                number.
	 44        *   Data             The actual sound data.


	NOTE TO READERS:

	The thing that makes reading wav files tricky is that java has no unsigned types.  This means that the
	binary data can't just be read and cast appropriately.  Also, we have to use larger types
	than are normally necessary.

	In many languages including java, an integer is represented by 4 bytes.  The issue here is
	that in most languages, integers can be signed or unsigned, and in wav files the  integers
	are unsigned.  So, to make sure that we can store the proper values, we have to use longs
	to hold integers, and integers to hold shorts.
	
	+Evan's original work didn't account for optional non PCM fmt header chunks. that's been fixed.
	+removed storing/reading of actual wav data, as this version is just for getting info about wav files.
	+added RIFF LIST INFO header parsing.
	
	 */
	
	// our private variables
	private String path;
	private int format;
	private long channels;
	private long sampleRate;
	private long byteRate;
	private int blockAlign;
	private int bitsPerSample;
	private long dataSize;
	private List<ListInfoDataPair> myInfo;
	private MP3File mp3Info;



	public int getFormat() {
		return format;
	}

	public long getChannels() {
		return channels;
	}

	public long getSampleRate() {
		return sampleRate;
	}

	public int getBlockAlign() {
		return blockAlign;
	}

	public int getBitsPerSample() {
		return bitsPerSample;
	}

	public long getDataSize() {
		return dataSize;
	}

	public AbstractID3v2 getID3v2Tags() {
		return mp3Info != null && mp3Info.hasID3v2Tag() ? mp3Info.getID3v2Tag() : null;
	}

	//get into set if available
	public List<ListInfoDataPair> getInfo() {
		return myInfo;
	}

	public boolean hasInfo() {
		return !(myInfo == null);
	}
	
	public String getInfo(String name) {
		if(myInfo == null)
			return null;
		name = name.toLowerCase();
		for(ListInfoDataPair ld : myInfo) {
			if(ld.dType.toLowerCase().equals(name))
				return ld.data;
		}
		return null;
	}
	
	public String getInfo(InfoTypes infoType) {
		if(myInfo == null)
			return null;
		for(ListInfoDataPair ld : myInfo) {
			if(ld.dType.equals(infoType.getValue()))
				return ld.data;
		}
		return null;
	}
		
	// get/set for the Path property
	public String getPath()
	{
		return path;
	}
	public void setPath(String newPath)
	{
		path = newPath;
	}
	
	//IVRs from Avaya platforms supports just a few audio combinations
	//this one is popular and works well
	public boolean isIVRFormat() {
		if(byteRate != 8000)
			return false;
		if(channels != 1)
			return false;
		if(format != 7)
			return false;
		if(bitsPerSample != 8)
			return false;
		return true;
	}

	// empty constructor
	public WavInfo()
     {
		path = "";
     }

	// constructor takes a wav path
	public WavInfo(String filePath)
     {
		path = filePath;
		this.read();
     }
	
	// read a wav file into this class
	// called automatically with string constructor
	public boolean read()
	{
		byte[] tmpLong = new byte[4];
		byte[] tmpInt = new byte[2];

		try(DataInputStream inFile = new DataInputStream(new FileInputStream(path)))
		{
     		//Read the RIFF header, verify it's a RIFF file with a WAVE format.

			String chunkID = "" + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte();
			if(!chunkID.equals("RIFF")) {
				//System.out.println("RIFF header missing or file corrupted");
				return false;
			}

			inFile.read(tmpLong); // read the ChunkSize
			@SuppressWarnings("unused")
			long myChunkSize = byteArrayToLong(tmpLong);

			String format = "" + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte();
			if(!format.equals("WAVE")) {
				//System.out.println("not a WAVE format file or file corrupted");
				return false;
			}


			//optimization, don't bother parsing LIST fields if we've already found LIST INFO
			boolean hasReadInfo = false;			
			
			//look at all remaining chunks
			while(inFile.available() > 0) {
				String ChunkIDNext = "" + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte();
				inFile.read(tmpLong); // read the ChunkSize (it's from here out, skips the 8 of this size and the name above.
				long chunkSize = byteArrayToLong(tmpLong);

				if(ChunkIDNext.equals("data")){
					dataSize = chunkSize;
					inFile.skipBytes((int) dataSize);
				} else if(ChunkIDNext.equals("fmt ")){
					//read fmt chunk

					inFile.read(tmpInt); // read the audio format.  This should be 1 for PCM
					this.format = byteArrayToInt(tmpInt);
		
					inFile.read(tmpInt); // read the # of channels (1 or 2)
					channels = byteArrayToInt(tmpInt);
					
					inFile.read(tmpLong); // read the samplerate
					sampleRate = byteArrayToLong(tmpLong);
		
					inFile.read(tmpLong); // read the byterate
					byteRate = byteArrayToLong(tmpLong);
		
					inFile.read(tmpInt); // read the blockalign
					blockAlign = byteArrayToInt(tmpInt);
		
					inFile.read(tmpInt); // read the bitspersample
					bitsPerSample = byteArrayToInt(tmpInt);
					
					//if PCM we are are down with fmt,
					//if not PMC need to read 2 bytes and check the extra param size, then read those
					if(this.format != 1) {
						inFile.read(tmpInt); // read the extraParamSize
						int extraParamSize = byteArrayToInt(tmpInt);
						while(extraParamSize-- > 0) {
							inFile.skipBytes(1);
						}
					}
				} else if(ChunkIDNext.equals("LIST") && !hasReadInfo) {
					// read the list type ID
					String ChunkLISTTypeID = "" + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte();
					//next is all the data, if the type was "INFO" then we can read it.
					if(!ChunkLISTTypeID.equals("INFO")) {
						//skip the rest of this LIST block, it's not an INFO block
						inFile.skipBytes((int)chunkSize - 4);
					} else {
						//if it is INFO
						myInfo = ListInfoDataPair.getData(inFile, (int)chunkSize);
						hasReadInfo = true;
					}
				} else if(ChunkIDNext.equals("id3 ")){
					byte[] id3data = inFile.readNBytes((int)chunkSize);

					try {
						mp3Info = new MP3File(new RandomArrayAccessor(id3data));
					} catch (TagException ignored) { }
				}
				//if this chunk is a type we aren't looking for, just skip it.
				else {
					inFile.skipBytes((int)chunkSize);
				}
			}	
		
		}
		catch(Exception e)
		{
			System.out.println("error occured");
			e.printStackTrace();
			return false;
		}

		return true; // this should probably be something more descriptive
	}
	
	
	//===========================
	// UTILITLES
	//===========================
	
	// return a printable summary of the wav file
	public String getSummary()
	{
		//String newline = System.getProperty("line.separator");
		String newline = "\r\n";
		String summary = "Format: " + format + newline + "Channels: " + channels + newline + "SampleRate: " + sampleRate + newline + "ByteRate: " + byteRate + newline + "BlockAlign: " + blockAlign + newline + "BitsPerSample: " + bitsPerSample + newline + "DataSize: " + dataSize;
		
		if(myInfo != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(newline + "LIST INFO CHUCK");
			String indent = "    ";
			for(ListInfoDataPair ld : myInfo) {
				InfoTypes InfoType = InfoTypes.forCode(ld.dType);
				String descriptionType = "";
				if(InfoType != null)
					descriptionType = "(" + InfoType.toString() + ")";
				sb.append(newline +indent +  ld.dType + descriptionType +"->" + ld.data);
			}
			summary += sb.toString();
		}
	
		return summary;
	}
	
	
	//===========================
	//CONVERT BYTES TO JAVA TYPES
	//===========================

		// these two routines convert a byte array to a unsigned short
		public static int byteArrayToInt(byte[] b)
		{
			int start = 0;
			int low = b[start] & 0xff;
			int high = b[start+1] & 0xff;
			return (int)( high << 8 | low );
		}


		// these two routines convert a byte array to an unsigned integer
		public static long byteArrayToLong(byte[] b)
		{
			int start = 0;
			int i = 0;
			int len = 4;
			int cnt = 0;
			byte[] tmp = new byte[len];
			for (i = start; i < (start + len); i++)
			{
				tmp[cnt] = b[i];
				cnt++;
			}
			long accum = 0;
			i = 0;
			for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 )
			{
				accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
				i++;
			}
			return accum;
		}


	//===========================
	//CONVERT JAVA TYPES TO BYTES
	//===========================
		
		// returns a byte array of length 4
		@SuppressWarnings("unused")
		private static byte[] intToByteArray(int i)
		{
			byte[] b = new byte[4];
			b[0] = (byte) (i & 0x00FF);
			b[1] = (byte) ((i >> 8) & 0x000000FF);
			b[2] = (byte) ((i >> 16) & 0x000000FF);
			b[3] = (byte) ((i >> 24) & 0x000000FF);
			return b;
		}

		// convert a short to a byte array
		@SuppressWarnings("unused")
		public static byte[] shortToByteArray(short data)
		{
			return new byte[]{(byte)(data & 0xff),(byte)((data >>> 8) & 0xff)};
		}
		
}
