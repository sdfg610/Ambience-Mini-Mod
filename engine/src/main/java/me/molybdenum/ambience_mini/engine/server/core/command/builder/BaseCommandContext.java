package me.molybdenum.ambience_mini.engine.server.core.command.builder;

import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;

import java.util.function.Supplier;

public abstract class BaseCommandContext {
    protected final Supplier<BaseServerCore<?, ?, ?, ?>> core;


    public BaseCommandContext(Supplier<BaseServerCore<?, ?, ?, ?>> core) {
        this.core = core;
    }


    public BaseServerCore<?, ?, ?, ?> getServer() {
        return core.get();
    }

    public abstract String getArgAsString(String name);

    public abstract void sendSuccess(Text text);
    public abstract void sendFailure(Text text);
}
