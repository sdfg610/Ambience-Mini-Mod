package me.molybdenum.ambience_mini.state.detectors;

public record Score(double tagScore, double materialScore, double lightingScore, boolean isSkyward) {
    public double sum() {
        return tagScore + materialScore + lightingScore;
    }
}
