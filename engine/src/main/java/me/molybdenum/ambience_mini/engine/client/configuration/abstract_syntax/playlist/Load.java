package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.playlist;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression.BoolLit;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression.FloatLit;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression.IntLit;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression.StringLit;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.misc.ArgList;

public record Load(StringLit file, ArgList args, int line) implements Playlist
{
    public static final String ARG_GAIN = "gain";  // Float or int
    public static final String ARG_LOOP = "loop";  // Bool

    public boolean getBoolArg(String ident, boolean defaultValue) {
        return args.stream()
                .filter(arg -> arg.ident().value().equals(ident))
                .findFirst()
                .map(arg -> ((BoolLit)arg.expr()).value())
                .orElse(defaultValue);
    }

    public int getIntArg(String ident, int defaultValue) {
        return args.stream()
                .filter(arg -> arg.ident().value().equals(ident))
                .findFirst()
                .map(arg -> ((IntLit)arg.expr()).value())
                .orElse(defaultValue);
    }

    public float getFloatArg(String ident, float defaultValue) {
        return args.stream()
                .filter(arg -> arg.ident().value().equals(ident))
                .findFirst()
                .map(arg -> arg.expr() instanceof FloatLit fl ? fl.value() : (float)((IntLit)arg.expr()).value())
                .orElse(defaultValue);
    }
}
