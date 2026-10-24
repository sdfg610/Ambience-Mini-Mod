package me.molybdenum.ambience_mini.deps.JavaWavInfo;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

public class ListInfoDataPair {
		
	public ListInfoDataPair(String dType, int dLength, String data) {
		this(dType, dLength, data, 0);
	}
	
	protected ListInfoDataPair(String dType, int dLength, String data, int consumedBytes) {
		this.dType = dType;
		this.dLength = dLength;
		this.consumedBytes = consumedBytes;
		this.data = data;
	}
	
	public String dType;
	public int dLength;
	public String data;
	//this is just for stream upkeep
	public int consumedBytes;
	
	public static ListInfoDataPair readData(DataInputStream inFile) {
		try {
			int consumedBytes = 0;
			byte[] tmpLong = new byte[4];
			String ID = "" + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte() + (char)inFile.readByte();
			consumedBytes+=4;
			inFile.read(tmpLong);
			int textSize = WavInfo.byteArrayToInt(tmpLong);
			consumedBytes+=4;
			String value = textSize == 0 ? "" : new String(inFile.readNBytes(textSize)).trim();
			consumedBytes+=textSize;
			return new ListInfoDataPair(ID, textSize, value, consumedBytes);
		}
		catch(Exception ex) {
			return null;
		}
	}
	
	public static List<ListInfoDataPair> getData(DataInputStream inFile, int ListSize){
		ArrayList<ListInfoDataPair> retList = new ArrayList<ListInfoDataPair>();
		int ListSizeCounter = (int) (ListSize - 4); //listSize counts the "INFO" string
		while(ListSizeCounter > 0) {
			ListInfoDataPair ld = ListInfoDataPair.readData(inFile);
			ListSizeCounter -= ld.consumedBytes;
			retList.add(ld);
		}
		return retList;
	}
}
