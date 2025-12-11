package me.molybdenum.ambience_mini.engine.configuration.music_provider;

import me.molybdenum.ambience_mini.engine.utils.Result;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public interface MusicProvider
{
    boolean exists(String musicPath);
    InputStream getMusicStream(String musicPath) throws FileNotFoundException;


    static Result<String> validatePath(String filePath) {
        String pathSeparator = FileSystems.getDefault().getSeparator();
        filePath = filePath.replace("\\", pathSeparator).replace("/", pathSeparator);

        Path path;
        try {
            path = Path.of(filePath);
        } catch (InvalidPathException ignored) {
            return Result.fail("The path '" + filePath + "' is invalid.");
        }

        if (path.isAbsolute())
            return Result.fail("Music paths must be relative. The path '" + path + "' is not.");

        for (Path value : path)
            if (value.toString().equals(".."))
                return Result.fail("Music paths cannot contain '..'-directories");

        return Result.of(path.toString());
    }
}
