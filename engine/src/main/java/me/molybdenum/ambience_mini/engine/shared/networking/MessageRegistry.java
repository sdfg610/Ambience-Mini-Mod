package me.molybdenum.ambience_mini.engine.shared.networking;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional.*;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.*;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.*;
import me.molybdenum.ambience_mini.engine.shared.utils.Result;

import java.util.HashMap;
import java.util.function.Supplier;

public class MessageRegistry {
    private static final HashMap<Integer, Supplier<AmMessage>> ID_TO_CONSTRUCTOR = new HashMap<>();
    private static final HashMap<Class<?>, Integer> CLASS_TO_ID = new HashMap<>();


    static {
        ////
        //// Absolutely do not change IDs after a release! This would break backwards compatibility.
        ////

        // Success and failure
        registerMessage(1, SuccessMessage::new, SuccessMessage.class);
        registerMessage(2, FailureMessage::new, FailureMessage.class);

        // Basic info
        registerMessage(10, ClientInfoMessage::new, ClientInfoMessage.class);

        // Combat
        registerMessage(20, MobTargetMessage::new, MobTargetMessage.class);

        // Areas
        registerMessage(30, CreateAreaMessage::new, CreateAreaMessage.class);
        registerMessage(31, PutAreaMessage::new, PutAreaMessage.class);
        registerMessage(32, DeleteAreaMessage::new, DeleteAreaMessage.class);
        registerMessage(33, RequestAreasMessage::new, RequestAreasMessage.class);

        // Name cache
        registerMessage(40, PutNameCacheMessage::new, PutNameCacheMessage.class);
        registerMessage(41, GetNameCacheMessage::new, GetNameCacheMessage.class);
    }

    private static <T extends AmMessage> void registerMessage(int id, Supplier<T> create, Class<T> clazz) {
        Object obj1 = ID_TO_CONSTRUCTOR.put(id, create::get);
        if (obj1 != null)
            throw new RuntimeException("Duplicate registration of message id '" + id + "'. This should not happen in production...");

        Object obj2 = CLASS_TO_ID.put(clazz, id);
        if (obj2 != null)
            throw new RuntimeException("Duplicate registration of message with class '" + clazz.getName() + "'. This should not happen in production...");
    }


    public static Result<AmMessage> createFromId(int id) {
        Supplier<AmMessage> supplier = ID_TO_CONSTRUCTOR.get(id);
        return supplier == null
                ? Result.fail("Could not find a message type with id '" + id + "'")
                : Result.of(supplier.get());
    }

    public static <T extends AmMessage> int idFromClass(Class<T> clazz) {
        Integer id = CLASS_TO_ID.get(clazz);
        if (id == null)
            throw new RuntimeException("Could not find a message type with class '" + clazz.getName() + "'. This should not happen in production...");
        return id;
    }
}
