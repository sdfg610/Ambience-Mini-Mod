package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.misc;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression.Expr;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.playlist.IdentP;

public record Arg(IdentP ident, Expr expr) { }
