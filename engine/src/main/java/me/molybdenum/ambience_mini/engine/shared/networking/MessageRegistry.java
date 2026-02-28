package me.molybdenum.ambience_mini.engine.shared.networking;

import me.molybdenum.ambience_mini.engine.shared.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.bidirectional.*;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_client.*;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.CreateAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.networking.messages.to_server.ModVersionMessage;

import java.util.HashMap;
import java.util.function.Supplier;

public class MessageRegistry {
    private static final HashMap<Integer, Supplier<AmMessage>> ID_TO_CONSTRUCTOR = new HashMap<>();
    private static final HashMap<Class<?>, Integer> CLASS_TO_ID = new HashMap<>();


    static {
        // Absolutely do not change IDs after a release! This would break backwards compatibility.
        registerMessage(1, FailureMessage::new, FailureMessage.class);
        registerMessage(2, ModVersionMessage::new, ModVersionMessage.class);

        registerMessage(3, MobTargetMessage::new, MobTargetMessage.class);

        registerMessage(4, CreateAreaMessage::new, CreateAreaMessage.class);
        registerMessage(5, PutAreaMessage::new, PutAreaMessage.class);
        registerMessage(6, DeleteAreaMessage::new, DeleteAreaMessage.class);
    }

    private static <T extends AmMessage> void registerMessage(int id, Supplier<T> create, Class<T> clazz) {
        Object obj1 = ID_TO_CONSTRUCTOR.put(id, create::get);
        if (obj1 != null)
            throw new RuntimeException("Duplicate registration of message id '" + id + "'");

        Object obj2 = CLASS_TO_ID.put(clazz, id);
        if (obj2 != null)
            throw new RuntimeException("Duplicate registration of message with class '" + clazz.getName() + "'");
    }


    public static AmMessage createFromId(int id) {
        Supplier<AmMessage> supplier = ID_TO_CONSTRUCTOR.get(id);
        if (supplier == null)
            throw new RuntimeException("Could not find a message type with id '" + id + "'");
        return supplier.get();
    }

    public static <T extends AmMessage> int idFromClass(Class<T> clazz) {
        Integer id = CLASS_TO_ID.get(clazz);
        if (id == null)
            throw new RuntimeException("Could not find a message type with class '" + clazz.getName() + "'");
        return id;
    }
}
