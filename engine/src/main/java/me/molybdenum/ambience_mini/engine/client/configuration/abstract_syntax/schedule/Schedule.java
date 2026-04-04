package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.schedule;

public sealed interface Schedule permits Block, Interrupt, Let, Play, When { }
