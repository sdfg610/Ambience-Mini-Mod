package me.molybdenum.ambience_mini.engine.shared.configuration.interpreter;

import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.kinds.AccessibleV;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.kinds.IndexableV;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.shared.music.Music;
import me.molybdenum.ambience_mini.engine.shared.music.music_provider.BaseMusicProvider;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BaseInterpreter {
    protected final BaseGameStateProvider gameStateProvider;


    public BaseInterpreter(BaseGameStateProvider gameStateProvider) {
        this.gameStateProvider = gameStateProvider;
    }


    protected Value<?> evalExpr(Expr expr, VariableEnv env) {
        if (expr instanceof Ident ident)
            return env.lookup(ident.value());
        else if (expr instanceof UndefinedLit)
            return new UndefinedVal();
        else if (expr instanceof BoolLit boolLit)
            return new BoolVal(boolLit.value());
        else if (expr instanceof IntLit intLit)
            return new IntVal(intLit.value());
        else if (expr instanceof FloatLit floatLit)
            return new FloatVal(floatLit.value());
        else if (expr instanceof StringLit stringLit)
            return new StringVal(stringLit.value());
        else if (expr instanceof PlaylistLit playlistLit)
            return evalPlaylist(playlistLit);
        else if (expr instanceof ValueLit valueLit)
            return valueLit.value();
        else if (expr instanceof GetEvent getEvent)
            return gameStateProvider.getEvent(getEvent.eventName().value()).isActive();
        else if (expr instanceof GetProperty property)
            return gameStateProvider.getProperty(property.propertyName().value()).getValue();
        else if (expr instanceof UnaryOp unOp)
            return evalUnOp(unOp, env);
        else if (expr instanceof BinaryOp binOp)
            return evalBinOp(binOp, env);
        else if (expr instanceof Accessor acc)
            return evalAccessor(acc, env);
        else if (expr instanceof QuantifierOp quanOp)
            return evalQuantifierOp(quanOp, env);

        throw new RuntimeException("Unhandled Expr-type '" + expr.getClass().getCanonicalName() + "' in evaluator. Please report this error to the developer");
    }

    protected PlaylistVal evalPlaylist(PlaylistLit playlistLit) {
        var music = new ArrayList<Music>();

        for (var load : playlistLit.music()) {
            String musicPath = BaseMusicProvider.validatePath(load.file().value()).getValue();
            float gain = load.getFloatArg(Music.ARG_GAIN, 0f);
            boolean doLoop = load.getBoolArg(Music.ARG_LOOP, false);
            int loopstart = load.getIntArg(Music.ARG_LOOPSTART, -1);
            int loopend = load.getIntArg(Music.ARG_LOOPEND, -1);
            int looplength = load.getIntArg(Music.ARG_LOOPLENGTH, -1);
            music.add(new Music(musicPath, gain, doLoop, loopstart, loopend, looplength));
        }

        return new PlaylistVal(music);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Unary operations
    protected Value<?> evalUnOp(UnaryOp unOp, VariableEnv env) {
        Value<?> val = evalExpr(unOp.expr(), env);

        return switch (unOp.op()) {
            case NOT -> new BoolVal(val.mapBool(b -> !b));
            case NEG -> opNeg(val);
        };
    }

    protected Value<?> opNeg(Value<?> value) {
        var i = value.asInt();
        if (i.isPresent())
            return new IntVal(-i.get());

        var f = value.asFloat();
        return f.isPresent() ? new FloatVal(f.get()) : UndefinedVal.INSTANCE;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Binary operations
    protected Value<?> evalBinOp(BinaryOp binOp, VariableEnv env) {
        Value<?> left = evalExpr(binOp.left(), env);
        Supplier<Value<?>> right = () -> evalExpr(binOp.right(), env);

        return switch (binOp.op()) {
            case EQ      -> new BoolVal( left.equals(right.get()) );
            case APP_EQ  -> opAppEq(left, right.get());
            case MATCH   -> opMatch(left, right.get());
            case AND     -> opAnd(left, right);
            case OR      -> opOr(left, right);
            case LT      -> opLt(left, right.get());
            case LE      -> opLe(left, right.get());
            case INDEXER -> opIndex(left, right.get());
            case ADD     -> opAdd(left, right.get());
            case SUB     -> opSub(left, right.get());
            case MUL     -> opMul(left, right.get());
            case DIV     -> opDiv(left, right.get());
            case APPEND  -> opAppend(left, right.get());
            case NULL_CHECK -> opNullCheck(left, right);
        };
    }

    protected BoolVal opAppEq(Value<?> v1, Value<?> v2) {
        return new BoolVal(
                v1.mapString(s1 -> v2.mapString(s1::contains))
        );
    }

    protected BoolVal opMatch(Value<?> v1, Value<?> v2) {
        return new BoolVal(
                v1.mapString(s1 -> v2.mapString(s2 -> {
                    try {
                        return s1.matches(s2);
                    } catch (Exception e) {
                        return false;
                    }
                }))
        );
    }

    protected BoolVal opAnd(Value<?> left, Supplier<Value<?>> right) {
        return new BoolVal(left.mapBool(b1 ->
                b1 ? right.get().asBool().orElse(null) : Boolean.FALSE
        ));
    }

    protected BoolVal opOr(Value<?> left, Supplier<Value<?>> right) {
        return new BoolVal(left.mapBool(b1 ->
                b1 ? Boolean.TRUE : right.get().asBool().orElse(null)
        ));
    }

    protected BoolVal opLt(Value<?> left, Value<?> right) {
        return new BoolVal(
                left instanceof FloatVal || right instanceof FloatVal
                        ? left.mapFloat(f1 -> right.mapFloat(f2 -> f1 < f2))
                        : left.mapInt(f1 -> right.mapInt(f2 -> f1 < f2))
        );
    }

    protected BoolVal opLe(Value<?> left, Value<?> right) {
        return new BoolVal(
                left instanceof FloatVal || right instanceof FloatVal
                        ? left.mapFloat(f1 -> right.mapFloat(f2 -> f1 <= f2))
                        : left.mapInt(f1 -> right.mapInt(f2 -> f1 <= f2))
        );
    }

    protected Value<?> opIndex(Value<?> base, Value<?> index) {
        return base instanceof IndexableV indexable
                ? indexable.getIndex(index)
                : UndefinedVal.INSTANCE;
    }

    protected Value<?> opAdd(Value<?> left, Value<?> right) {
        try {
            if (left instanceof StringVal)
                return left.mapString(s1 -> right.mapString(s2 -> new StringVal(s1 + s2)));

            return left instanceof FloatVal || right instanceof FloatVal
                    ? left.mapFloat(f1 -> right.mapFloat(f2 -> new FloatVal(f1 + f2)))
                    : left.mapInt(i1 -> right.mapInt(i2 -> new IntVal(i1 + i2)));
        }
        catch (Exception ignored) {
            return UndefinedVal.INSTANCE;
        }
    }

    protected Value<?> opSub(Value<?> left, Value<?> right) {
        try {
            return left instanceof FloatVal || right instanceof FloatVal
                    ? left.mapFloat(f1 -> right.mapFloat(f2 -> new FloatVal(f1 - f2)))
                    : left.mapInt(i1 -> right.mapInt(i2 -> new IntVal(i1 - i2)));
        }
        catch (Exception ignored) {
            return UndefinedVal.INSTANCE;
        }
    }

    protected Value<?> opMul(Value<?> left, Value<?> right) {
        try {
            return left instanceof FloatVal || right instanceof FloatVal
                    ? left.mapFloat(f1 -> right.mapFloat(f2 -> new FloatVal(f1 * f2)))
                    : left.mapInt(i1 -> right.mapInt(i2 -> new IntVal(i1 * i2)));
        }
        catch (Exception ignored) {
            return UndefinedVal.INSTANCE;
        }
    }

    protected Value<?> opDiv(Value<?> left, Value<?> right) {
        try {
            return left instanceof FloatVal || right instanceof FloatVal
                    ? left.mapFloat(f1 -> right.mapFloat(f2 -> new FloatVal(f1 / f2)))
                    : left.mapInt(i1 -> right.mapInt(i2 -> new IntVal(i1 / i2)));
        }
        catch (Exception ignored) {
            return UndefinedVal.INSTANCE;
        }
    }

    protected Value<?> opAppend(Value<?> left, Value<?> right) {
        try {
            var pl1 = left.asMusicList().orElse(null);
            var pl2 = right.asMusicList().orElse(null);
            if (pl1 == null)
                return new PlaylistVal(pl2);
            else if (pl2 == null)
                return new PlaylistVal(pl1);

            var music = new ArrayList<Music>(pl1.size() + pl2.size());
            music.addAll(pl1);
            music.addAll(pl2);

            return new PlaylistVal(music);
        }
        catch (Exception ignored) {
            return UndefinedVal.INSTANCE;
        }
    }

    protected Value<?> opNullCheck(Value<?> left, Supplier<Value<?>> right) {
        return left.isUndefined() ? right.get() : left;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Accessors
    protected Value<?> evalAccessor(Accessor acc, VariableEnv env) {
        return evalExpr(acc.base(), env) instanceof AccessibleV accessible
                ? accessible.getField(acc.field().value())
                : UndefinedVal.INSTANCE;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Quantifier operations
    protected BoolVal evalQuantifierOp(QuantifierOp quanOp, VariableEnv env) {
        String ident = quanOp.identifier().value();
        Predicate<Value<?>> evaluator = elem -> evalExpr(quanOp.condition(), env.enterScope().bind(ident, elem)).asBool().orElse(false);

        return new BoolVal(
                evalExpr(quanOp.list(), env).mapList(list ->
                        switch (quanOp.quantifier()) {
                            case ALL -> list.stream().allMatch(evaluator);
                            case ANY -> list.stream().anyMatch(evaluator);
                        }
                )
        );
    }
}
