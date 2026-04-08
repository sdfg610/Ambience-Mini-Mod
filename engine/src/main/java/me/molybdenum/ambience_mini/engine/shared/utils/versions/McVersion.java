package me.molybdenum.ambience_mini.engine.shared.utils.versions;

public enum McVersion {
    V1_18,
    V1_19,
    V1_20,
    V1_21,
    V26,
    ANY

    ;

    public boolean greaterThanOrEqual(McVersion other) {
        return this.ordinal() >= other.ordinal();
    }
}
