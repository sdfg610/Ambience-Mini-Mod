package me.molybdenum.ambience_mini.engine.loader.abstract_syntax.conf;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.schedule.Schedule;

public record ScheduleDecl(Schedule schedule) implements Config { }
