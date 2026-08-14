package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.schedule;

public sealed interface Schedule permits Block, Interrupt, Let, Play, Vanilla, When
{
    int line();
}
