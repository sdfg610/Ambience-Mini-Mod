package me.molybdenum.ambience_mini.engine.server.core.flags;

import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.StringVal;
import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.Constants;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FlagManager {
    private final Object lock = new Object();

    private final ArrayList<Consumer<FlagOperation>> updateListeners = new ArrayList<>();

    private final ConcurrentHashMap<String, StringVal> idToValue = new ConcurrentHashMap<>();

    private Logger logger;
    private FlagStorage flagStorage;

    private boolean isDirty = false;

    private ScheduledFuture<?> saveFuture;
    private int saveIntervalMillis = 60_000;


    @SuppressWarnings("rawtypes")
    public void init(BaseServerCore core) {
        if (this.logger != null)
            throw new RuntimeException("Multiple calls to 'BaseServerAreaManager.init'!");

        this.logger = core.logger;
        this.flagStorage = new FlagStorage(logger, core.getAmStoragePath());

        // TODO: Configurable interval
    }


    public void registerPeriodicTasks(ScheduledExecutorService executor) {
        saveFuture = executor.scheduleAtFixedRate(
                this::saveFlags,
                0, saveIntervalMillis, TimeUnit.MILLISECONDS
        );
    }

    public void stopPeriodicTasks() {
        saveFuture.cancel(true);
    }


    public List<Map.Entry<String, StringVal>> getFlags() {
        return new ArrayList<>(idToValue.entrySet());
    }

    public StringVal getFlag(String id) {
        return idToValue.get(id);
    }


    public Text createFlag(String id, String value) {
        if (idToValue.containsKey(id))
            return AmLang.MSG_FLAG_ALREADY_EXISTS.text(id);
        if (!validateId(id))
            return Text.ofTranslatable(AmLang.MSG_FLAG_ID_INVALID, id, Integer.toString(Constants.MAX_FLAG_ID_LENGTH));
        if (!validateValue(value))
            return Text.ofTranslatable(AmLang.MSG_FLAG_VALUE_INVALID, value, Integer.toString(Constants.MAX_FLAG_VALUE_LENGTH));
        idToValue.put(id, new StringVal(value));
        fireUpdateEvent(new FlagOperation.Put(id, value));
        isDirty = true;
        return null;
    }

    public Text updateFlag(String id, String value) {
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
        Text error = idToValue.remove(id) == null ? AmLang.MSG_FLAG_NOT_EXISTS.text(id) : null;
        if (error == null) {
            isDirty = true;
            fireUpdateEvent(new FlagOperation.Delete(id));
        }
        return error;
    }


    public void loadFlags() {
        synchronized (lock) {
            idToValue.clear();
            flagStorage.loadFlagsInto(idToValue);
            isDirty = false;
        }
    }

    public void saveFlags() {
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
