package me.molybdenum.ambience_mini.engine.configuration.music_provider;

import me.molybdenum.ambience_mini.engine.utils.Result;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.regex.Pattern;

public interface MusicProvider
{
    boolean exists(String musicPath);
    InputStream getMusicStream(String musicPath) throws FileNotFoundException;


    static Result<String> validatePath(String filePath) {
        String pathSeparator = getPathSeparator();
        filePath = filePath.replace("\\", pathSeparator).replace("/", pathSeparator);

        Path path;
        try {
            path = Path.of(filePath);
        } catch (InvalidPathException ignored) {
            return Result.fail("The path '" + filePath + "' is invalid.");
        }

        if (path.isAbsolute())
            return Result.fail("Music paths must be relative. The path '" + path + "' is not.");

        var parts = path.toString().split(Pattern.quote(pathSeparator));
        for (String part : parts)
            if (part.equals(".."))
                return Result.fail("Music paths cannot contain '..'-directories");

        return Result.of(path.toString());
    }

    private static String getPathSeparator() {
        try {
            return FileSystems.getDefault().getSeparator(); // Throws Exception in Ambience IDE
        } catch (Exception ignored) {
            return "/";
        }
    }
}
