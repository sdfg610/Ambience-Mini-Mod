package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

public sealed interface Expr permits Accessor, BinaryOp, BoolLit, FloatLit, GetEvent, GetProperty, Ident, IntLit, PlaylistLit, QuantifierOp, StringLit, UnaryOp, UndefinedLit, ValueLit {
    int line();
}
