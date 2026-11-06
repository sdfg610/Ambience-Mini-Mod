package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.Expr;

public record Modifier(String name, Expr arg, int line, ModifierKinds modifier) implements Command
{
    public Modifier(String name, Expr arg, int line) {
        this(name, arg, line, ModifierKinds.ofName(name));
    }

    public Modifier withArg(Expr newArg) {
        return new Modifier(name, newArg, line, modifier);
    }
}
