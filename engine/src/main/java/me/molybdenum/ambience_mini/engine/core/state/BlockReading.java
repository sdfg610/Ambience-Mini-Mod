package me.molybdenum.ambience_mini.engine.core.state;

public record BlockReading<TBlockPos, TBlockState>(
        TBlockPos blockPos,
        TBlockState blockState,
        double xRot,
        double yRot  // Rotation from the perspective of the player
) { }
