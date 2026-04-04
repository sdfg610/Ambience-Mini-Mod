package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression;

public sealed interface Expr permits BinaryOp, BoolLit, FloatLit, GetEvent, GetProperty, IdentE, IntLit, Accessor, QuantifierOp, StringLit, UnaryOp, UndefinedLit { }
