package me.molybdenum.ambience_mini.engine.shared.core.areas;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AreaStorage {
    private static final String NAME_PATTERN = "^.*_areas\\.json$";

    private final Logger logger;
    private final Path basePath;


    public AreaStorage(Logger logger, Path basePath) {
        this.logger = logger;
        this.basePath = basePath;
    }

    public Optional<List<Area>> loadAllAreas() {
        File storageDir = basePath.toFile();
        if (!storageDir.isDirectory())
            return Optional.empty();

        File[] fileEntries = storageDir.listFiles();
        if (fileEntries == null)
            return Optional.empty();

        ArrayList<Area> areas = new ArrayList<>();
        for (File entry : fileEntries)
            if (entry.isFile() && entry.getName().matches(NAME_PATTERN)) {
                var res = loadAreasFrom(entry);
                if (res.isPresent())
                    areas.addAll(res.get());
                else {
                    doBackup(fileEntries);
                    return Optional.empty();
                }
            }

        return Optional.of(areas);
    }

    public Optional<List<Area>> loadAreasFrom(File file) {
        Path path = file.toPath();
        try {
            JsonElement json = JsonParser.parseReader(Files.newBufferedReader(path, StandardCharsets.UTF_8));
            if (json.isJsonArray())
                return Optional.ofNullable(parseAreas(path, json.getAsJsonArray()));
            logger.error("Malformed area file does not contain a list of areas: {}", path);
        } catch (Exception e) {
            logger.error("Failed to load areas from file: '{}'", path);
        }
        return Optional.empty();
    }

    private List<Area> parseAreas(Path file, JsonArray list) {
        ArrayList<Area> areas = new ArrayList<>(list.size());
        for (var elem : list)
            if (Area.validateJson(elem))
                areas.add(Area.fromJson(elem.getAsJsonObject()));
            else {
                logger.error("Could not load JSON element as area in file '{}'. The element was:\n{}", file, elem);
                return null;
            }
        return areas;
    }


    public void saveAreas(List<Area> areas, String dimensionID) {
        JsonArray jsonArray = new JsonArray();
        areas.forEach(area -> jsonArray.add(area.toJson()));

        Path path = basePath.resolve(dimensionID.replace(':', '+') + "_areas.json");
        if (!jsonArray.isEmpty()) {
            try {
                if (!basePath.toFile().exists())
                    Files.createDirectories(basePath);
                Files.writeString(path, jsonArray.toString(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                String err = "Could not save area-file to storage location: " + path;
                logger.error(err, e);
            }
        }
        else if (path.toFile().exists()) {
            try {
                Files.delete(path);
            } catch (Exception e) {
                String err = "Could not delete area-file from storage location: " + path;
                logger.error(err, e);
            }
        }
    }


    private void doBackup(File[] fileEntries) {
        try {
            String dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(':', '_').replace('.', '_');
            Path areaBackupPath = basePath.resolve("area_backup_" + dateTime);
            Files.createDirectory(areaBackupPath);
            for (File entry : fileEntries)
                if (entry.isFile() && entry.getName().matches(NAME_PATTERN)) {
                    Path path = entry.toPath();
                    Files.copy(path, areaBackupPath.resolve(path.getFileName().toString()));
                }
        } catch (Exception e) {
            logger.error("Could not make backup of areas...", e);
        }
    }
}
