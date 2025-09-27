package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.shed;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr.Expr;

public record When(Expr condition, Shed body) implements Shed { }
