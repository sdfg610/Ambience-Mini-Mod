package me.molybdenum.ambience_mini.engine.server.core.flags;

public interface FlagOperation {
    record Put(String id, String value) implements FlagOperation { }
    record Delete(String id) implements FlagOperation { }
}
