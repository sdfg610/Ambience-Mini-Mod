package me.molybdenum.ambience_mini.v1_19_2.server.core.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.molybdenum.ambience_mini.engine.server.core.BaseServerCore;
import me.molybdenum.ambience_mini.engine.server.core.command.builder.BaseCommandContext;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.Supplier;

public class AmCommandContext extends BaseCommandContext
{
    private final CommandContext<CommandSourceStack> ctx;


    public AmCommandContext(
            Supplier<BaseServerCore<?, ?, ?, ?>> serverCore,
            CommandContext<CommandSourceStack> ctx
    ) {
        super(serverCore);
        this.ctx = ctx;
    }


    @Override
    public void sendSuccess(Text text) {
        ctx.getSource().sendSuccess(ofText(text), true);
    }

    @Override
    public void sendFailure(Text text) {
        ctx.getSource().sendSuccess(ofText(text), true);
    }

    @Override
    public String getArgAsString(String name) {
        return StringArgumentType.getString(ctx, name);
    }


    private Component ofText(Text text) {
        return text.isLiteral
                ? Component.literal(text.base)
                : Component.translatable(text.base, Arrays.stream(text.args).toArray());
    }
}
