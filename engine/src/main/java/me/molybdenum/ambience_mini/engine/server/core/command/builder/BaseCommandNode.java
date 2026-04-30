package me.molybdenum.ambience_mini.engine.server.core.command.builder;

import java.util.function.Function;

public abstract class BaseCommandNode<T, S>
{
    protected final T builder;
    protected final Object builderObj;


    public BaseCommandNode(T builder) {
        this.builder = builder;
        this.builderObj = builder;
    }


    public BaseCommandNode<T, S> then(BaseCommandNode<T, S> child) {
        innerThen(child.builder);
        return this;
    }

    public BaseCommandNode<T, S> requires(int permission) {
        innerRequires(permission);
        return this;
    }

    public BaseCommandNode<T, S> executes(Function<BaseCommandContext, Integer> command) {
        innerExecutes(command);
        return this;
    }

    protected abstract void innerThen(T child);
    protected abstract void innerRequires(int permission);
    protected abstract void innerExecutes(Function<BaseCommandContext, Integer> command);

    public abstract S complete();
}
