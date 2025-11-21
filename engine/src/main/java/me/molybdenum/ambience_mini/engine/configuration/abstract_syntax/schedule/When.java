package me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.schedule;

import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.expression.Expr;

public record When(Expr condition, Schedule body) implements Schedule { }
