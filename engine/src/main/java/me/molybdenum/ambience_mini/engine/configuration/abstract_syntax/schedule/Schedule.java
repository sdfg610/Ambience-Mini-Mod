package me.molybdenum.ambience_mini.engine.configuration.abstract_syntax.schedule;

public sealed interface Schedule permits Block, Interrupt, Play, When { }
