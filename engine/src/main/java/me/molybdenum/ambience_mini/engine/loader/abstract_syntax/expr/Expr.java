package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expr;

public sealed interface Expr permits BinaryOp, BoolV, Ev, FloatV, Get, IdentE, IntV, StringV { }
