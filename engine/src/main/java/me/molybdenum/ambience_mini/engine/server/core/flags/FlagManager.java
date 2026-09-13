package me.molybdenum.ambience_mini.engine.server.core.flags;

import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.StringVal;
import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.jobs.JobCenter;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;
import me.molybdenum.ambience_mini.engine.shared.utils.results.TextResult;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class FlagManager {
    private final Object lock = new Object();

    private final ArrayList<Consumer<FlagOperation>> updateListeners = new ArrayList<>();

    private final ConcurrentHashMap<String, StringVal> idToValue = new ConcurrentHashMap<>();

    private Logger logger;

    private FlagStorage flagStorage;
    private boolean isDirty = false;

    private boolean flagsDisabled;
    private int saveIntervalMillis;


    @SuppressWarnings("rawtypes")
    public void init(BaseServerCore core) {
        if (this.logger != null)
            throw new RuntimeException("Multiple calls to 'BaseServerAreaManager.init'!");

        this.logger = core.logger;
        this.flagStorage = new FlagStorage(logger, core.getAmStoragePath());

        this.flagsDisabled = !core.serverConfig.enableFlags.get();
        this.saveIntervalMillis = core.serverConfig.flagsSaveInterval.get();
    }


    public void registerPeriodicTasks(JobCenter executor) {
        executor.schedule(
                JobCenter.Job.of(this::saveFlags), 0, saveIntervalMillis
        );
    }


    public TextResult<List<Map.Entry<String, StringVal>>> getFlags() {
        if (flagsDisabled)
            return TextResult.fail(AmLang.MSG_FLAGS_DISABLED.text());
        return TextResult.of(new ArrayList<>(idToValue.entrySet()));
    }


    public Text createFlag(String id, String value) {
        if (flagsDisabled)
            return AmLang.MSG_FLAGS_DISABLED.text();
        if (idToValue.containsKey(id))
            return AmLang.MSG_FLAG_ALREADY_EXISTS.text(id);
        if (!validateId(id))
            return AmLang.MSG_FLAG_ID_INVALID.text(id, Integer.toString(Constants.MAX_FLAG_ID_LENGTH));
        if (!validateValue(value))
            return AmLang.MSG_FLAG_VALUE_INVALID.text(value, Integer.toString(Constants.MAX_FLAG_VALUE_LENGTH));

        idToValue.put(id, new StringVal(value));

        fireUpdateEvent(new FlagOperation.Put(id, value));
        isDirty = true;
        return null;
    }

    public Text updateFlag(String id, String value) {
        if (flagsDisabled)
            return AmLang.MSG_FLAGS_DISABLED.text();
        if (!idToValue.containsKey(id))
            return AmLang.MSG_FLAG_NOT_EXISTS.text(id);
        if (!validateValue(value))
            return Text.ofTranslatable(AmLang.MSG_FLAG_VALUE_INVALID, value, Integer.toString(Constants.MAX_FLAG_VALUE_LENGTH));

        idToValue.put(id, new StringVal(value));

        fireUpdateEvent(new FlagOperation.Put(id, value));
        isDirty = true;
        return null;
    }

    public Text deleteFlag(String id) {
        if (flagsDisabled)
            return AmLang.MSG_FLAGS_DISABLED.text();

        if (idToValue.remove(id) == null)
            return AmLang.MSG_FLAG_NOT_EXISTS.text(id);

        fireUpdateEvent(new FlagOperation.Delete(id));
        isDirty = true;
        return null;
    }


    public void loadFlags() {
        if (flagsDisabled)
            return;

        synchronized (lock) {
            idToValue.clear();
            flagStorage.loadFlagsInto(idToValue);
            isDirty = false;
        }
    }

    public void saveFlags() {
        if (flagsDisabled)
            return;

        synchronized (lock) {
            if (isDirty) {
                flagStorage.saveFlagsFrom(idToValue);
                isDirty = false;
            }
        }
    }


    public void fireUpdateEvent(FlagOperation op) {
        updateListeners.forEach(listener -> listener.accept(op));
    }

    public void addUpdateListener(Consumer<FlagOperation> listener) {
        updateListeners.add(listener);
    }

    public void removeUpdateListener(Consumer<FlagOperation> listener) {
        updateListeners.remove(listener);
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validateId(String id) {
        return id != null && !id.isEmpty() && id.chars().noneMatch(Character::isWhitespace) && id.length() <= Constants.MAX_FLAG_ID_LENGTH;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validateValue(String value) {
        return value != null && value.length() <= Constants.MAX_FLAG_VALUE_LENGTH;
    }
}
