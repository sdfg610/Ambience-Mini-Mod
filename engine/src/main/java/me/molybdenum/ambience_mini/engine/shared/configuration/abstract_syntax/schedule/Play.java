package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.schedule;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.Expr;
import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.expression.IntLit;

import java.util.Optional;

public record Play(Expr playlist, boolean isInstant, boolean ifdef, IntLit priority, int line) implements Schedule
{
    public Play withPlaylistAndPriority(Expr newPlaylist, IntLit newPriority) {
        return new Play(newPlaylist, isInstant, ifdef, newPriority, line);
    }


    public int getPriorityOrElse(int defaultPriority) {
        return priority == null ? defaultPriority : priority.value();
    }

    public int getPriority() {
        return priority.value();
    }

    public Optional<Integer> getPriorityOpt() {
        return Optional.ofNullable(priority == null ? null : priority.value());
    }

    public int getPriorityLine() {
        return priority == null ? -1 : priority.line();
    }

    public IntLit computePriorityIfAbsent(int defaultPriority) {
        return priority != null ? priority : new IntLit(defaultPriority, -1);
    }
}
