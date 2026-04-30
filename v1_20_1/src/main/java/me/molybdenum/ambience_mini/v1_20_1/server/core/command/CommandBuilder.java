package me.molybdenum.ambience_mini.v1_20_1.server.core.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.molybdenum.ambience_mini.engine.server.core.command.builder.BaseCommandNode;
import me.molybdenum.ambience_mini.engine.server.core.command.builder.BaseCommandContext;
import me.molybdenum.ambience_mini.v1_20_1.server.core.ServerCore;
import net.minecraft.commands.CommandSourceStack;

import java.util.function.Function;
import java.util.function.Supplier;

public class CommandBuilder extends BaseCommandNode<ArgumentBuilder<CommandSourceStack, ?>, LiteralArgumentBuilder<CommandSourceStack>>
{
    private final Supplier<ServerCore> core;


    public CommandBuilder(ArgumentBuilder<CommandSourceStack, ?> builder, Supplier<ServerCore> core) {
        super(builder);
        this.core = core;
    }


    @Override
    protected void innerThen(ArgumentBuilder<CommandSourceStack, ?> child) {
        builder.then(child);
    }

    @Override
    protected void innerRequires(int permission) {
        builder.requires(ctx -> ctx.hasPermission(permission));
    }

    @Override
    protected void innerExecutes(Function<BaseCommandContext, Integer> command) {
        builder.executes(ctx -> command.apply(new AmCommandContext(core::get, ctx)));
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> complete() {
        //noinspection unchecked
        return (LiteralArgumentBuilder<CommandSourceStack>) builder;
    }
}
