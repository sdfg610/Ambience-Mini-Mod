package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command.Block;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command.Command;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.Ident;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.StringLit;

public record Preset(StringLit name, Block body) {
    public Preset withBody(Command cmd) {
        return cmd == null ? null : new Preset(name, cmd.asBlock());
    }
}
