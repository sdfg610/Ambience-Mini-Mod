package me.molybdenum.ambience_mini.v1_21_1.server.core.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.molybdenum.ambience_mini.engine.server.core.command.builder.AmArgumentType;
import me.molybdenum.ambience_mini.engine.server.core.command.builder.BaseCommandNode;
import me.molybdenum.ambience_mini.engine.server.core.command.builder.BaseCommandNodeFactory;
import me.molybdenum.ambience_mini.v1_21_1.server.core.ServerCore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.function.Supplier;

public class CommandNodeFactory extends BaseCommandNodeFactory<ArgumentBuilder<CommandSourceStack, ?>, LiteralArgumentBuilder<CommandSourceStack>>
{
    private final Supplier<ServerCore> core;


    public CommandNodeFactory(Supplier<ServerCore> core) {
        this.core = core;
    }


    @Override
    public BaseCommandNode<ArgumentBuilder<CommandSourceStack, ?>, LiteralArgumentBuilder<CommandSourceStack>> literal(String name) {
        return new CommandBuilder(Commands.literal(name), core);
    }

    @Override
    public BaseCommandNode<ArgumentBuilder<CommandSourceStack, ?>, LiteralArgumentBuilder<CommandSourceStack>> argument(String name, AmArgumentType type) {
        return new CommandBuilder(Commands.argument(name, ofAmArgType(type)), core);
    }


    private ArgumentType<?> ofAmArgType(AmArgumentType type) {
        return switch (type) {
            case WORD -> StringArgumentType.word();
            case STRING -> StringArgumentType.string();
        };
    }
}
