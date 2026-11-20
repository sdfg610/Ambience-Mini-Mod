package me.molybdenum.ambience_mini.deps.JavaWavInfo;

public enum InfoTypes {

	ARTIST("IART"),
	COPYRIGHT("ICOP"),
	TRACKTITLE("INAM"),
	ALBUMTITLE("IPRD"),
	TRACKNUM("ITRK"),
	YEAR("ICRD"),
	GENRE("IGNR"),
	SOFTWARE("ISFT"),
	COMMENT("ICMT");

    private final String value;

    InfoTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public static InfoTypes forCode(String code) {
    	InfoTypes[] valuesList = InfoTypes.values();
    	for(InfoTypes it : valuesList) {
    		if(it.getValue().equals(code))
    			return it;
    	}
        return null;
     }
}