package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.misc.ArgList;

import java.util.ArrayList;

public record PlaylistLit(ArrayList<Load> music, int line) implements Expr
{
    public PlaylistLit(int line) {
        this(new ArrayList<>(), line);
    }

    public PlaylistLit withMusic(ArrayList<Load> newMusic) {
        return new PlaylistLit(newMusic, line);
    }


    public record Load(StringLit file, ArgList args, int line)
    {
        public Load withArgs(ArgList newArgs) {
            return new Load(file, newArgs, line);
        }


        public boolean getBoolArg(String ident, boolean defaultValue) {
            return args.stream()
                    .filter(arg -> arg.ident().value().equals(ident))
                    .findFirst()
                    .flatMap(arg -> ((ValueLit)arg.expr()).value().asBool())
                    .orElse(defaultValue);
        }

        public int getIntArg(String ident, int defaultValue) {
            return args.stream()
                    .filter(arg -> arg.ident().value().equals(ident))
                    .findFirst()
                    .flatMap(arg -> ((ValueLit)arg.expr()).value().asInt())
                    .orElse(defaultValue);
        }

        public float getFloatArg(String ident, float defaultValue) {
            return args.stream()
                    .filter(arg -> arg.ident().value().equals(ident))
                    .findFirst()
                    .map(arg -> ((ValueLit)arg.expr()).value())
                    .flatMap(val -> val.asFloat().or(() -> val.asInt().map(Integer::floatValue)))
                    .orElse(defaultValue);
        }
    }
}
