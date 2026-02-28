package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression;

public sealed interface Expr permits BinaryOp, BoolLit, GetEvent, FloatLit, GetProperty, IdentE, IntLit, QuantifierOp, StringLit { }
