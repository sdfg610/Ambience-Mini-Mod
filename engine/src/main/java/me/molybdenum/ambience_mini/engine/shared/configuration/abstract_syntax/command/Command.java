package me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command;

public sealed interface Command permits Block, Interrupt, Let, Modifier, Play, Vanilla, When
{
    int line();
}
