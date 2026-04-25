package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.schedule;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.expression.IntLit;
import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.playlist.Playlist;

public record Play(Playlist playlist, boolean isInstant, IntLit priority) implements Schedule {
    public int getPriorityOrElse(int defaultPriority) {
        return priority == null ? defaultPriority : priority.value();
    }

    public int getPriority() {
        return priority.value();
    }

    public int getPriorityLine() {
        return priority == null ? -1 : priority.line();
    }

    public IntLit computePriorityIfAbsent(int defaultPriority) {
        return priority != null ? priority : new IntLit(defaultPriority, -1);
    }
}
