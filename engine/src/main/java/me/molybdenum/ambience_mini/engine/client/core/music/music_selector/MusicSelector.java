package me.molybdenum.ambience_mini.engine.client.core.music.music_selector;

import me.molybdenum.ambience_mini.engine.client.core.music.music_selector.selection.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.BaseInterpreter;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.schedule.*;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.VariableEnv;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.Value;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiConsumer;

public class MusicSelector extends BaseInterpreter
{
    private final Schedule optimizedSchedule;

    private int uniqueId = 0;
    private int nestedInterrupts = 0;


    public MusicSelector(
            Config config,
            BaseGameStateProvider gameStateProvider
    ) {
        super(config, gameStateProvider);
        this.optimizedSchedule = optimizeSchedule(config.schedule());
    }


    public void prepare(@Nullable ArrayList<String> messages) {
        gameStateProvider.prepare(messages);
    }

    public @NotNull Selection selectPlaylist(@Nullable ArrayList<Pair<String, Value<?>>> trace) {
        BiConsumer<String, Value<?>> tracer =
                trace == null ? null : (name, val) -> trace.add(new Pair<>(name, val));

        try {
            if (tracer != null)
                gameStateProvider.registerOnFiredListener(tracer);
            var selection = evalSchedule(optimizedSchedule, rootEnv.enterScope());
            return selection == null ? new NoneSelection() : selection;
        } finally {
            if (tracer != null)
                gameStateProvider.unregisterOnFiredListener(tracer);
        }

    }


    // -----------------------------------------------------------------------------------------------------------------
    // Initialization
    private Schedule optimizeSchedule(Schedule schedule) {
        if (schedule instanceof Play play){
            var playlist = play.playlist();
            if (!(playlist instanceof Ident) && isConst(playlist)) {
                // Pre-compute playlist so we don't need to later.
                String name = '\'' + (uniqueId++) + "-playlist";
                rootEnv.bind(name, evalExpr(play.playlist(), rootEnv));
                playlist = new Ident(name, -1);
            }
            return new Play(playlist, play.isInstant(), play.ifdef(), play.computePriorityIfAbsent(nestedInterrupts), play.line());
        }
        else if (schedule instanceof Vanilla) {
            return schedule;
        }
        else if (schedule instanceof Interrupt interrupt) {
            ++nestedInterrupts;
            var body = optimizeSchedule(interrupt.body());
            --nestedInterrupts;
            return body;
        }
        else if (schedule instanceof Let let) {
            return new Let(let.type(), let.ident(), let.value(), optimizeSchedule(let.body()), let.line());
        }
        else if (schedule instanceof Block block) {
            return new Block(
                    block.body().stream()
                            .map(this::optimizeSchedule)
                            .toList(),
                    block.line()
            );
        }
        else if (schedule instanceof When when)
            return new When(when.condition(), optimizeSchedule(when.body()), when.line());
        else
            throw new RuntimeException("Unhandled Schedule-type '" + schedule.getClass().getCanonicalName() + "'. Please report this error to the developer");
    }

    public boolean isConst(Expr expr) {
        if (expr instanceof Ident ident)
            return rootEnv.isBound(ident.value());
        else if (expr instanceof UndefinedLit
                || expr instanceof BoolLit
                || expr instanceof IntLit
                || expr instanceof FloatLit
                || expr instanceof StringLit
                || expr instanceof Playlist
        )
            return true;
        else if (expr instanceof UnaryOp unOp)
            return isConst(unOp.expr());
        else if (expr instanceof BinaryOp binOp)
            return isConst(binOp.left()) && isConst(binOp.right());
        else
            return false;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Playlist selection
    private Selection evalSchedule(Schedule schedule, VariableEnv env) {
        if (schedule instanceof Play play) {
            return evalExpr(play.playlist(), env)
                    .asMusicList()
                    .<Selection>map(musicList -> new PlaylistSelection(musicList, play.isInstant(), play.getPriority(), play.line()))
                    .orElse(play.ifdef() ? null : new UndefinedSelection(play.line()));
        }
        else if (schedule instanceof Vanilla vanilla) {
            return new VanillaSelection(vanilla.line());
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
}
