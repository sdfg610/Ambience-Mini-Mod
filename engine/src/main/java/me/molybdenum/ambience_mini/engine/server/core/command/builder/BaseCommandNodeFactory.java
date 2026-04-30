package me.molybdenum.ambience_mini.engine.server.core.command.builder;

public abstract class BaseCommandNodeFactory<T, S> {
    public abstract BaseCommandNode<T, S> literal(String name);
    public abstract BaseCommandNode<T, S> argument(String name, AmArgumentType type);
}
