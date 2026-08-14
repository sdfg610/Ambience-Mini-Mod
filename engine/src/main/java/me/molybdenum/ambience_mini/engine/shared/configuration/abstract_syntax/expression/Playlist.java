package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.misc.ArgList;

import java.util.ArrayList;

public record Playlist(ArrayList<Load> music, int line) implements Expr
{
    public Playlist(int line) {
        this(new ArrayList<>(), line);
    }


    public record Load(StringLit file, ArgList args, int line)
    {
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
}
