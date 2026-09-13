package me.molybdenum.ambience_mini.engine.shared.music.music_provider;

import me.molybdenum.ambience_mini.engine.shared.music.Music;
import me.molybdenum.ambience_mini.engine.shared.utils.results.StrResult;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public abstract class BaseMusicProvider
{
    protected final String basePath;


    public BaseMusicProvider(String basePath) {
        this.basePath = basePath;
    }


    //------------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract boolean existsLocally(String musicPath);
    public abstract List<Path> listLocalMusicFiles();

    public abstract int getLocalMusicSize(String musicPath) throws FileNotFoundException;
    public abstract InputStream getLocalMusicStream(String musicPath) throws FileNotFoundException;

    public abstract int getServerMusicSize(String musicPath) throws FileNotFoundException;
    public abstract InputStream getServerMusicStream(String musicPath) throws FileNotFoundException;


    //------------------------------------------------------------------------------------------------------------------
    // Concrete API
    public int getMusicSize(Music music) throws FileNotFoundException {
        return music.locatedOnServer() ? getServerMusicSize(music.path()) : getLocalMusicSize(music.path());
    }

    public InputStream getMusicStream(Music music) throws FileNotFoundException {
        return music.locatedOnServer() ? getServerMusicStream(music.path()) : getLocalMusicStream(music.path());
    }


    public String getBasePath() {
        return basePath;
    }

    public Path getLocalPath(String musicPath) {
        return Path.of(basePath, musicPath);
    }


    //------------------------------------------------------------------------------------------------------------------
    // Static API
    public static StrResult<String> validatePath(String filePath) {
        String pathSeparator = getPathSeparator();
        filePath = filePath.replace("\\", pathSeparator).replace("/", pathSeparator);

        Path path;
        try {
            path = Path.of(filePath);
        } catch (InvalidPathException ignored) {
            return StrResult.fail("The path '" + filePath + "' is invalid.");
        }

        if (path.isAbsolute())
            return StrResult.fail("Music paths must be relative. The path '" + path + "' is not.");

        var parts = path.toString().split(Pattern.quote(pathSeparator));
        for (String part : parts)
            if (part.equals(".."))
                return StrResult.fail("Music paths cannot contain '..'-directories");

        return StrResult.of(path.toString());
    }

    private static String getPathSeparator() {
        try {
            return FileSystems.getDefault().getSeparator(); // Throws Exception in Ambience IDE
        } catch (Exception ignored) {
            return "/";
        }
    }
}
