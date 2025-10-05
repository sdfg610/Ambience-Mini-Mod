package me.molybdenum.ambience_mini.state.detectors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

// TODO: Remove blockPos and state
public record Score(double tagScore, double materialScore, double lightingScore, boolean isSkyward, BlockPos blockPos, BlockState state) {
    public double sum() {
        return  tagScore + materialScore + lightingScore;
    }
}
