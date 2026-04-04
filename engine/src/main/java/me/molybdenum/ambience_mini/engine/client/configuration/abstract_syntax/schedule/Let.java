package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.schedule;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.Type;
import org.jetbrains.annotations.Nullable;

public record Let(@Nullable Type type, IdentE ident, Expr value, Schedule body, int line) implements Schedule {
}
