package me.molybdenum.ambience_mini.engine.shared.core.networking;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.CreateAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.DeleteAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.GetAreasMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.areas.PutAreaMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.name_cache.GetNameCacheMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.name_cache.PutNameCacheMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.ClientInfoMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.FailureMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.SuccessMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.combat.MobTargetMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags.DeleteFlagMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags.GetFlagsMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.flags.PutFlagMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.structures.GetStructuresMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.structures.PutChunkReferencesMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.structures.PutChunkStructuresMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.utils.Result;

import java.util.HashMap;
import java.util.function.Function;

public class MessageRegistry {
    private static final HashMap<Integer, Function<AmReader, AmMessage>> ID_TO_CONSTRUCTOR = new HashMap<>();
    private static final HashMap<Class<?>, Integer> CLASS_TO_ID = new HashMap<>();


    static {
        ////
        //// Absolutely do not change IDs after a release! This would break backwards compatibility.
        ////

        // Success and failure
        registerMessage(1, SuccessMessage::new, SuccessMessage.class); // 2.5.0
        registerMessage(2, FailureMessage::new, FailureMessage.class); // 2.5.0

        // Basic info
        registerMessage(10, ClientInfoMessage::new, ClientInfoMessage.class); // 2.5.0

        // Combat
        registerMessage(20, MobTargetMessage::new, MobTargetMessage.class); // 2.5.0

        // Areas
        registerMessage(30, CreateAreaMessage::new, CreateAreaMessage.class); // 2.5.0
        registerMessage(31, PutAreaMessage::new, PutAreaMessage.class); // 2.5.0
        registerMessage(32, DeleteAreaMessage::new, DeleteAreaMessage.class); // 2.5.0
        registerMessage(33, GetAreasMessage::new, GetAreasMessage.class); // 2.5.0

        // Name cache
        registerMessage(40, PutNameCacheMessage::new, PutNameCacheMessage.class); // 2.5.0
        registerMessage(41, GetNameCacheMessage::new, GetNameCacheMessage.class); // 2.5.0

        // Structures
        registerMessage(50, GetStructuresMessage::new, GetStructuresMessage.class); // 2.5.0
        registerMessage(51, PutChunkReferencesMessage::new, PutChunkReferencesMessage.class); // 2.5.0
        registerMessage(52, PutChunkStructuresMessage::new, PutChunkStructuresMessage.class); // 2.5.0

        // Flags
        registerMessage(60, GetFlagsMessage::new, GetFlagsMessage.class); // 2.6.0
        registerMessage(61, PutFlagMessage::new, PutFlagMessage.class); // 2.6.0
        registerMessage(62, DeleteFlagMessage::new, DeleteFlagMessage.class); // 2.6.0
    }

    private static <T extends AmMessage> void registerMessage(int id, Function<AmReader, T> create, Class<T> clazz) {
        Object obj1 = ID_TO_CONSTRUCTOR.put(id, create::apply);
        if (obj1 != null)
            throw new RuntimeException("Duplicate registration of message id '" + id + "'. This should not happen in production...");

        Object obj2 = CLASS_TO_ID.put(clazz, id);
        if (obj2 != null)
            throw new RuntimeException("Duplicate registration of message with class '" + clazz.getName() + "'. This should not happen in production...");
    }


    public static Result<AmMessage> createFromId(int id, AmReader reader) {
        Function<AmReader, AmMessage> constructor = ID_TO_CONSTRUCTOR.get(id);
        return constructor == null
                ? Result.fail("Could not find a message type with id '" + id + "'")
                : Result.of(constructor.apply(reader));
    }

    public static <T extends AmMessage> int idFromClass(Class<T> clazz) {
        Integer id = CLASS_TO_ID.get(clazz);
        if (id == null)
            throw new RuntimeException("Could not find a message type with class '" + clazz.getName() + "'. This should not happen in production...");
        return id;
    }
}
