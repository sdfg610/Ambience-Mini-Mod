package me.molybdenum.ambience_mini.deps.JavaWavInfo;

public enum EncodingTypes {

	PCM("PCM", 0x1),
	ADPCM("ADPCM", 0x2),
	IEEEFLOAT("IEEE float",0x3),
	VSELP("VSELP",0x4),
	IBMCVSD("IBM CVSD",0x5),
	ALAW("a-law G.711 non-us",0x6),
	ULAW("u-law G.711 us/japan", 0x7),
	DTS("DTS", 0x8);

    private final String name;
    private final int value;

    EncodingTypes(String name, int value) {
    	this.name = name;
    	this.value = value;
    }

    public String getName() {
    	return name;
    }
    
    public int getValue() {
        return value;
    }
    
    public static EncodingTypes forCode(int code) {
    	EncodingTypes[] valuesList = EncodingTypes.values();
    	for(EncodingTypes it : valuesList) {
    		if(it.getValue() == code)
    			return it;
    	}
        return null;
     }
   
}

//wav format types / encoding tag
//being lazy

//0x9 = DRM 
//0x10 = OKI-ADPCM 
//0x11 = IMA-ADPCM 
//0x12 = Mediaspace ADPCM 
//0x13 = Sierra ADPCM 
//0x14 = G723 ADPCM 
//0x15 = DIGISTD 
//0x16 = DIGIFIX 
//0x30 = Dolby AC2 
//0x31 = GSM610 
//0x3b = Rockwell ADPCM 
//0x3c = Rockwell DIGITALK 
//0x40 = G721 ADPCM 
//0x41 = G728 CELP 
//0x50 = MPEG 
//0x52 = RT24 
//0x53 = PAC 
//0x55 = MP3 
//0x64 = G726 ADPCM 
//0x65 = G722 ADPCM 
//0x101 = IBM u-Law 
//0x102 = IBM a-Law 
//0x103 = IBM ADPCM 
//0xffff = Development