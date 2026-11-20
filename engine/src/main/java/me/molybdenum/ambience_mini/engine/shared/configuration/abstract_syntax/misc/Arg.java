package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.misc;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.Expr;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.Ident;

public record Arg(Ident ident, Expr expr) { }
