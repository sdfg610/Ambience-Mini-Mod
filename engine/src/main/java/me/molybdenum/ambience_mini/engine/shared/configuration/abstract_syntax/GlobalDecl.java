package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.Expr;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.Ident;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.Type;

public record GlobalDecl(Type type, Ident ident, Expr value) { }
