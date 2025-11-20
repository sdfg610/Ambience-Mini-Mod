package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.schedule;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.expression.Expr;

public record When(Expr condition, Schedule body) implements Schedule { }
