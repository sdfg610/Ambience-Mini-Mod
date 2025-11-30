package me.molybdenum.ambience_mini.engine.configuration.interpreter;

import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.config.*;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.playlist.*;
import me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.schedule.*;
import me.molybdenum.ambience_mini.engine.configuration.interpreter.values.*;
import me.molybdenum.ambience_mini.engine.configuration.Music;
import me.molybdenum.ambience_mini.engine.core.providers.BaseGameStateProvider;
import me.molybdenum.ambience_mini.engine.utils.Pair;
import me.molybdenum.ambience_mini.engine.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Interpreter
{
    private final String musicDirectory;
    private final BaseGameStateProvider gameStateProvider;
    private final Schedule schedule;

    private final VariableEnv rootEnv = VariableEnv.empty();

    private int uniqueId = 0;



    public Interpreter(Config config, String musicDirectory, BaseGameStateProvider gameStateProvider) {
        this.musicDirectory = musicDirectory;
        this.gameStateProvider = gameStateProvider;
        this.schedule = initConf(config);
    }


    public void prepare(@Nullable ArrayList<String> messages) {
        gameStateProvider.prepare(messages);
    }

    public PlaylistChoice selectPlaylist(@Nullable ArrayList<Pair<String, Value>> trace) {
        @SuppressWarnings("DataFlowIssue")
        BiConsumer<String, Value> tracer = (name, val) -> trace.add(new Pair<>(name, val));

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
            throw new RuntimeException("Unhandled Conf-type: " + config.getClass().getCanonicalName());
    }

    private Schedule initSchedule(Schedule schedule) {
        if (schedule instanceof Play play){
            if (play.playlist() instanceof IdentP)
                return schedule; // Already on simplest form, just return this identifier.
            else {
                // Pre-compute playlist so we don't need to later.
                String name = '\'' + (uniqueId++) + "-playlist";
                rootEnv.bind(name, evalPlaylist(play.playlist(), rootEnv));
                return new Play(new IdentP(name), play.isInstant());
            }
        }
        else if (schedule instanceof Interrupt interrupt) {
            return new Interrupt(initSchedule(interrupt.body()));
        }
        else if (schedule instanceof Block block) {
            return new Block(
                    block.body().stream()
                            .map(this::initSchedule)
                            .toList()
            );
        }
        else if (schedule instanceof When when)
            return new When(when.condition(), initSchedule(when.body()));
        else
            throw new RuntimeException("Unhandled Schedule-type in initialization: " + schedule.getClass().getCanonicalName());
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Playlist selection
    private PlaylistChoice evalSchedule(Schedule schedule, VariableEnv env) {
        if (schedule instanceof Play play) {
            return new PlaylistChoice(evalPlaylist(play.playlist(), env), false, play.isInstant());
        }
        else if (schedule instanceof Interrupt interrupt) {
            PlaylistChoice result = evalSchedule(interrupt.body(), env);
            if (result != null)
                result = result.asInterrupt();
            return result;
        }
        else if (schedule instanceof Block block) {
            return block.body().stream()
                    .map(sc -> evalSchedule(sc, env))
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
        }
        else if (schedule instanceof When when) {
            return evalExpr(when.condition(), env).asBoolean()
                    ? evalSchedule(when.body(), env)
                    : null;
        }
        else
            throw new RuntimeException("Unhandled Schedule-type in evaluator: " + schedule.getClass().getCanonicalName());
    }

    private List<Music> evalPlaylist(Playlist play, VariableEnv env) {
        if (play instanceof IdentP ident)
            return env.lookup(ident.value());
        else if (play instanceof Concat concat)
            return Stream.concat(
                    evalPlaylist(concat.left(), env).stream(),
                    evalPlaylist(concat.right(), env).stream()
            ).toList();
        else if (play instanceof Load load)
            return List.of(new Music(Path.of(musicDirectory, load.file().value()), load.gain() != null ? load.gain().value() : 0f));
        else if (play instanceof Nil)
            return List.of();
        else
            throw new RuntimeException("Unhandled Playlist-type in evaluator: " + play.getClass().getCanonicalName());
    }

    private Value evalExpr(Expr expr, VariableEnv env) {
        if (expr instanceof IdentE identE)
            return env.lookup(identE.value());
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
        else if (expr instanceof BinaryOp binOp)
            return evalBinOp(binOp, env);
        else if (expr instanceof QuantifierOp quanOp)
            return evalQuantifierOp(quanOp, env);

        throw new RuntimeException("Unhandled Expr-type in evaluator: " + expr.getClass().getCanonicalName());
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Binary operations
    private Value evalBinOp(BinaryOp binOp, VariableEnv env) {
        Value left = evalExpr(binOp.left(), env);

        return switch (binOp.op()) {
            case EQ     -> new BoolVal( opEQ(left, evalExpr(binOp.right(), env)) );
            case APP_EQ -> new BoolVal( left.asString().contains(evalExpr(binOp.right(), env).asString()) );
            case AND    -> new BoolVal( left.asBoolean() && evalExpr(binOp.right(), env).asBoolean() );
            case OR     -> new BoolVal( left.asBoolean() || evalExpr(binOp.right(), env).asBoolean() );
            case LT     -> new BoolVal( opLE(left, evalExpr(binOp.right(), env)) );
        };
    }

    private boolean opEQ(Value leftVal, Value rightVal) {
        if (leftVal instanceof BoolVal left && rightVal instanceof BoolVal right)
            return left.value == right.value;
        else if (leftVal instanceof IntVal left && rightVal instanceof IntVal right)
            return left.value == right.value;
        else if (leftVal instanceof FloatVal left && rightVal instanceof FloatVal right)
            return left.value == right.value;
        else if (leftVal instanceof StringVal left && rightVal instanceof StringVal right)
            return left.value.equals(right.value);
        else if (leftVal instanceof ListVal left && rightVal instanceof ListVal right)
            return Utils.zip(left.value, right.value).allMatch(pair -> opEQ(pair.left(), pair.right()));
        else
            throw new RuntimeException("Eq operation could not handle values of type: " + leftVal.getClass().getName() + " and " + rightVal.getClass().getName());
    }

    private boolean opLE(Value left, Value right) {
        if (left instanceof FloatVal || right instanceof FloatVal)
            return left.asFloat() < right.asFloat();
        else
            return left.asInt() < right.asInt();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Quantifier operations
    private Value evalQuantifierOp(QuantifierOp quanOp, VariableEnv env) {
        String ident = quanOp.identifier();
        Predicate<Value> evaluator = elem -> evalExpr(quanOp.condition(), env.enterScope().bind(ident, elem)).asBoolean();

        List<Value> list = evalExpr(quanOp.list(), env).asList();
        return switch (quanOp.quantifier()) {
            case ALL -> new BoolVal(list.stream().allMatch(evaluator));
            case ANY -> new BoolVal(list.stream().anyMatch(evaluator));
        };
    }
}
