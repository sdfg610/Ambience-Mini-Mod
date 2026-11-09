package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command;

import java.util.List;

public sealed interface Command permits Block, Interrupt, Let, Modifier, Play, Vanilla, When
{
    int line();

    default Block asBlock() {
        return this instanceof Block blk ? blk : new Block(List.of(this), this.line());
    }
}
