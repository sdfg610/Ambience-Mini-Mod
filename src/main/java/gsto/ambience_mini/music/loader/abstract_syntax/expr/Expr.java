package gsto.ambience_mini.music.loader.abstract_syntax.expr;

public sealed interface Expr permits BinaryOp, BoolV, Event, FloatV, Get, IdentE, IntV, StringV { }
