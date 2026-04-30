package me.molybdenum.ambience_mini.engine.server.core.command;

import me.molybdenum.ambience_mini.engine.server.core.command.builder.BaseCommandNodeFactory;
import me.molybdenum.ambience_mini.engine.server.core.command.commands.FlagCommands;

public class CommandRegistry
{
    public static <T, S> S build(BaseCommandNodeFactory<T, S> bld) {
        return bld.literal("ambience_mini")
                .then(FlagCommands.register(bld))
                .complete();
    }
}
