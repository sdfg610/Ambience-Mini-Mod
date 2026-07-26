package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.config;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.schedule.Schedule;

public record ScheduleDecl(Schedule schedule) implements Config { }
