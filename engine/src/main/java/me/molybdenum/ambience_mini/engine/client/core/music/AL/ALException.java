package me.molybdenum.ambience_mini.engine.client.core.music.AL;

import static org.lwjgl.openal.AL10.alGetString;

public class ALException extends RuntimeException {
    public ALException(int errorCode) {
        super("AL error '" + errorCode + "': " + alGetString(errorCode));
    }
}
