package me.molybdenum.ambience_mini.engine.state.readers;

public record BlockReading<TBlockPos, TBlockState>(
        TBlockPos blockPos,
        TBlockState blockState,
        double xRot,
        double yRot
) { }
