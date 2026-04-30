package me.molybdenum.ambience_mini.engine.server.core.command.commands;

import me.molybdenum.ambience_mini.engine.server.core.command.builder.AmArgumentType;
import me.molybdenum.ambience_mini.engine.server.core.command.builder.BaseCommandContext;
import me.molybdenum.ambience_mini.engine.server.core.command.builder.BaseCommandNode;
import me.molybdenum.ambience_mini.engine.server.core.command.builder.BaseCommandNodeFactory;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;


public class FlagCommands {
    public static <T, S> BaseCommandNode<T, S> register(BaseCommandNodeFactory<T, S> bld) {
        return bld.literal("flag").requires(2).then(
                bld.literal("create").then(
                        bld.argument("id", AmArgumentType.WORD).then(
                                bld.argument("value", AmArgumentType.STRING).executes(FlagCommands::createFlag)
                        )
                )
        ).then(
                bld.literal("update").then(
                        bld.argument("id", AmArgumentType.WORD).then(
                                bld.argument("value", AmArgumentType.STRING).executes(FlagCommands::setFlag)
                        )
                )
        ).then(
                bld.literal("delete").then(
                        bld.argument("id", AmArgumentType.WORD).executes(FlagCommands::deleteFlag)
                )
        );
    }

    private static int createFlag(BaseCommandContext context) {
        String id = context.getArgAsString("id");
        String value = context.getArgAsString("value");

        var error = context.getServer().flagManager.createFlag(id, value);
        if (error != null) {
            context.sendFailure(error);
            return 0;
        } else {
            context.sendSuccess(Text.ofTranslatable(AmLang.MSG_FLAG_CREATED, id, value));
            return 1;
        }
    }

    private static int setFlag(BaseCommandContext context) {
        String id = context.getArgAsString("id");
        String value = context.getArgAsString("value");

        var error = context.getServer().flagManager.updateFlag(id, value);
        if (error != null) {
            context.sendFailure(error);
            return 0;
        } else {
            context.sendSuccess(Text.ofTranslatable(AmLang.MSG_FLAG_UPDATED, id, value));
            return 1;
        }
    }

    private static int deleteFlag(BaseCommandContext context) {
        String id = context.getArgAsString("id");

        var error = context.getServer().flagManager.deleteFlag(id);
        if (error != null) {
            context.sendFailure(error);
            return 0;
        } else {
            context.sendSuccess(Text.ofTranslatable(AmLang.MSG_FLAG_DELETED, id));
            return 1;
        }
    }
}
