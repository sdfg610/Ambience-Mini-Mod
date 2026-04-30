package me.molybdenum.ambience_mini.engine.server.core.flags;

import com.google.gson.*;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.StringVal;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

public class FlagStorage {
    private static final String FILE_NAME = "flags.json";

    private final Logger logger;
    private final Path basePath;
    private final Path flagsFilePath;


    public FlagStorage(Logger logger, Path basePath) {
        this.logger = logger;
        this.basePath = basePath;
        this.flagsFilePath = basePath.resolve(FILE_NAME);
    }


    public void loadFlagsInto(ConcurrentHashMap<String, StringVal> idToValue) {
        if (Files.exists(flagsFilePath)) {
            try {
                JsonElement json = JsonParser.parseReader(Files.newBufferedReader(flagsFilePath, StandardCharsets.UTF_8));
                if (validate(json))
                    json.getAsJsonArray().forEach(elem -> parseFlag(elem.getAsJsonObject(), idToValue));
            } catch (Exception e) {
                logger.error("Failed to load flags from file: '{}'", flagsFilePath);
                doBackup();
            }
        }
    }

    private boolean validate(JsonElement flags) {
        if (!flags.isJsonArray()) {
            logger.error("Malformed flag file does not contain a list of flags: {}", flagsFilePath);
            return false;
        }

        for (var elem : flags.getAsJsonArray())
            if ((!(elem instanceof JsonObject obj)) || !obj.has("id") || !obj.has("value")) {
                logger.error("Flag-file '{}' contains element that is not a proper flag: {}", flagsFilePath, elem);
                return false;
            }
            else if (!obj.get("id").isJsonPrimitive())
                logger.error("Flag-file '{}' contains element with malformed id: {}", flagsFilePath, elem);
            else if (!(obj.get("value").isJsonPrimitive() || obj.get("value").isJsonNull()))
                logger.error("Flag-file '{}' contains element with malformed value: {}", flagsFilePath, elem);

        return true;
    }

    private void parseFlag(JsonObject elem, ConcurrentHashMap<String, StringVal> idToValue) {
        var value = elem.get("value");
        idToValue.put(elem.get("id").getAsString(), new StringVal(value.isJsonNull() ? null : value.getAsString()));
    }


    public void saveFlagsFrom(ConcurrentHashMap<String, StringVal> idToValue) {
        JsonArray jsonArray = new JsonArray();
        idToValue.forEach((id, value) -> jsonArray.add(entryFromIdAndValue(id, value)));

        if (!jsonArray.isEmpty()) {
            try {
                if (!basePath.toFile().exists())
                    Files.createDirectories(basePath);
                Files.writeString(flagsFilePath, jsonArray.toString(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                String err = "Could not save area-file to storage location: " + flagsFilePath;
                logger.error(err, e);
            }
        }
        else if (flagsFilePath.toFile().exists()) {
            try {
                Files.delete(flagsFilePath);
            } catch (Exception e) {
                String err = "Could not delete area-file from storage location: " + flagsFilePath;
                logger.error(err, e);
            }
        }
    }

    private static JsonObject entryFromIdAndValue(String id, StringVal value) {
        var obj = new JsonObject();
        obj.add("id", new JsonPrimitive(id));
        obj.add("value", value.asString().map(val -> (JsonElement)new JsonPrimitive(val)).orElse(JsonNull.INSTANCE));
        return obj;
    }


    private void doBackup() {
        try {
            if (Files.exists(flagsFilePath)) {
                String dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(':', '_').replace('.', '_');
                Path backupPath = basePath.resolve("flags_backup-" + dateTime + ".json");
                Files.copy(flagsFilePath, backupPath);
            }
        } catch (Exception e) {
            logger.error("Could not make backup of flags...", e);
        }
    }
}
