package me.molybdenum.ambience_ide;

import me.molybdenum.ambience_mini.engine.configuration.Music;
import org.teavm.jso.JSExport;


public class Test
{
    @JSExport
    public static String getAmbienceMiniVersion() {
        return BuildConfig.APP_VERSION;
    }

    @JSExport
    public static Music musicTest() {
        return new Music("/test/path", 0.0f);
    }

    @JSExport
    public static String musicName(Music music) {
        return music.musicPath();
    }

    @JSExport
    public static String getInterpreter(String musicConfig) {
        return "";
    }
}
