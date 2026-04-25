package me.molybdenum.ambience_mini.engine.client.configuration.interpreter;

import me.molybdenum.ambience_mini.engine.client.configuration.Music;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.config.Config;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.config.PlaylistDecl;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.config.ScheduleDecl;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.playlist.*;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.schedule.*;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.*;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.kinds.AccessibleV;
import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.kinds.IndexableV;
import me.molybdenum.ambience_mini.engine.client.configuration.music_provider.MusicProvider;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Interpreter
{
    private final static UndefinedVal UNDEFINED = new UndefinedVal();

    private final BaseGameStateProvider gameStateProvider;
    private final Schedule schedule;

    private final VariableEnv rootEnv = VariableEnv.empty();

    private int uniqueId = 0;
    private int nestedInterrupts = 0;



    public Interpreter(
            Config config,
            BaseGameStateProvider gameStateProvider
    ) {
        this.gameStateProvider = gameStateProvider;
        this.schedule = initConf(config);
    }


    public void prepare(@Nullable ArrayList<String> messages) {
        gameStateProvider.prepare(messages);
    }

    public PlaylistChoice selectPlaylist(@Nullable ArrayList<Pair<String, Value<?>>> trace) {
        @SuppressWarnings("DataFlowIssue")
        BiConsumer<String, Value<?>> tracer = (name, val) -> trace.add(new Pair<>(name, val));

        if (trace != null) gameStateProvider.registerOnFiredListener(tracer);
        PlaylistChoice choice = evalSchedule(schedule, rootEnv.enterScope());
        if (trace != null) gameStateProvider.unregisterOnFiredListener(tracer);

        return choice;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Initialization
    private Schedule initConf(Config config) {
        if (config instanceof PlaylistDecl decl) {
            List<Music> playlist = evalPlaylist(decl.playlist(), rootEnv);
            rootEnv.bind(decl.ident().value(), playlist);
            return initConf(decl.config());
        }
        else if (config instanceof ScheduleDecl decl)
            return initSchedule(decl.schedule());
        else
            throw new RuntimeException("Unhandled Conf-type '" + config.getClass().getCanonicalName() + "' in initialization. Please report this error to the developer");
    }

    private Schedule initSchedule(Schedule schedule) {
        if (schedule instanceof Play play){
            var playlist = play.playlist();
            if (!(playlist instanceof IdentP)) {
                // Pre-compute playlist so we don't need to later.
                String name = '\'' + (uniqueId++) + "-playlist";
                rootEnv.bind(name, evalPlaylist(play.playlist(), rootEnv));
                playlist = new IdentP(name, -1);
            }
            return new Play(playlist, play.isInstant(), play.computePriorityIfAbsent(nestedInterrupts));
        }
        else if (schedule instanceof Interrupt interrupt) {
            ++nestedInterrupts;
            var body = initSchedule(interrupt.body());
            --nestedInterrupts;
            return body;
        }
        else if (schedule instanceof Let let) {
            return new Let(let.type(), let.ident(), let.value(), initSchedule(let.body()), let.line());
        }
        else if (schedule instanceof Block block) {
            return new Block(
                    block.body().stream()
                            .map(this::initSchedule)
                            .toList()
            );
        }
        else if (schedule instanceof When when)
            return new When(when.condition(), initSchedule(when.body()), when.line());
        else
            throw new RuntimeException("Unhandled Schedule-type '" + schedule.getClass().getCanonicalName() + "'. Please report this error to the developer");
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Playlist selection
    private PlaylistChoice evalSchedule(Schedule schedule, VariableEnv env) {
        if (schedule instanceof Play play) {
            return new PlaylistChoice(evalPlaylist(play.playlist(), env), play.isInstant(), play.getPriority());
        }
        else if (schedule instanceof Block block) {
            return block.body().stream()
                    .map(sc -> evalSchedule(sc, env))
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
        }
        else if (schedule instanceof When when) {
            return evalExpr(when.condition(), env).mapBool(
                    b -> b ? evalSchedule(when.body(), env) : null
            );
        }
        else if (schedule instanceof Let let) {
            return evalSchedule(
                    let.body(),
                    env.enterScope().bind(let.ident().value(), evalExpr(let.value(), env))
            );
        }
        else if (schedule instanceof Interrupt) {
            throw new RuntimeException("Interrupts should not show up explicitly in the initialized schedule. Please report this error to the developer");
        }
        else
            throw new RuntimeException("Unhandled Schedule-type '" + schedule.getClass().getCanonicalName() + "' in evaluator. Please report this error to the developer");
    }

    private List<Music> evalPlaylist(Playlist play, VariableEnv env) {
        if (play instanceof IdentP ident)
            return env.lookup(ident.value());
        else if (play instanceof Concat concat)
            return Stream.concat(
                    evalPlaylist(concat.left(), env).stream(),
                    evalPlaylist(concat.right(), env).stream()
            ).toList();
        else if (play instanceof Load load) {
            String musicPath = MusicProvider.validatePath(load.file().value()).getValue();
            float baseGain = load.gain() != null ? load.gain().value() : 0f;
            return List.of(new Music(musicPath, baseGain));
        }
        else if (play instanceof Nil)
            return List.of();
        else
            throw new RuntimeException("Unhandled Playlist-type '" + play.getClass().getCanonicalName() + "' in evaluator. Please report this error to the developer");
    }

    private Value<?> evalExpr(Expr expr, VariableEnv env) {
        if (expr instanceof IdentE identE)
            return env.lookup(identE.value());
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


    // -----------------------------------------------------------------------------------------------------------------
    // Unary operations
    private Value<?> evalUnOp(UnaryOp unOp, VariableEnv env) {
        Value<?> val = evalExpr(unOp.expr(), env);

        return switch (unOp.op()) {
            case NOT -> new BoolVal(val.mapBool(b -> !b));
            case NEG -> opNeg(val);
        };
    }

    private Value<?> opNeg(Value<?> value) {
        var i = value.asInt();
        if (i.isPresent())
            return new IntVal(-i.get());

        var f = value.asFloat();
        return f.isPresent() ? new FloatVal(f.get()) : UNDEFINED;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Binary operations
    private Value<?> evalBinOp(BinaryOp binOp, VariableEnv env) {
        Value<?> left = evalExpr(binOp.left(), env);
        Supplier<Value<?>> right = () -> evalExpr(binOp.right(), env);

        return switch (binOp.op()) {
            case EQ     -> new BoolVal( left.equals(right.get()) );
            case APP_EQ -> opAppEq(left, right.get());
            case MATCH  -> opMatch(left, right.get());
            case AND    -> opAnd(left, right);
            case OR     -> opOr(left, right);
            case LT     -> opLt(left, right.get());
            case LE     -> opLe(left, right.get());
            case INDEXER -> opIndex(left, right.get());
        };
    }

    private BoolVal opAppEq(Value<?> v1, Value<?> v2) {
        return new BoolVal(
                v1.mapString(s1 -> v2.mapString(s1::contains))
        );
    }

    private BoolVal opMatch(Value<?> v1, Value<?> v2) {
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

    private BoolVal opAnd(Value<?> left, Supplier<Value<?>> right) {
        return new BoolVal(left.mapBool(b1 ->
            b1 ? right.get().asBool().orElse(null) : Boolean.FALSE
        ));
    }

    private BoolVal opOr(Value<?> left, Supplier<Value<?>> right) {
        return new BoolVal(left.mapBool(b1 ->
                b1 ? Boolean.TRUE : right.get().asBool().orElse(null)
        ));
    }

    private BoolVal opLt(Value<?> left, Value<?> right) {
        return new BoolVal(
                left instanceof FloatVal || right instanceof FloatVal
                        ? left.mapFloat(f1 -> right.mapFloat(f2 -> f1 < f2))
                        : left.mapInt(f1 -> right.mapInt(f2 -> f1 < f2))
        );
    }

    private BoolVal opLe(Value<?> left, Value<?> right) {
        return new BoolVal(
                left instanceof FloatVal || right instanceof FloatVal
                        ? left.mapFloat(f1 -> right.mapFloat(f2 -> f1 <= f2))
                        : left.mapInt(f1 -> right.mapInt(f2 -> f1 <= f2))
        );
    }

    private Value<?> opIndex(Value<?> base, Value<?> index) {
        return base instanceof IndexableV indexable
                ? indexable.getIndex(index)
                : UNDEFINED;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Accessors
    private Value<?> evalAccessor(Accessor acc, VariableEnv env) {
        return evalExpr(acc.base(), env) instanceof AccessibleV accessible
                ? accessible.getField(acc.field().value())
                : UNDEFINED;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Quantifier operations
    private BoolVal evalQuantifierOp(QuantifierOp quanOp, VariableEnv env) {
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
