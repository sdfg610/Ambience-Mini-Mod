package me.molybdenum.ambience_mini.engine.shared.core.features;

public enum Availability {
    NOT_SUPPORTED, ENABLED, DISABLED

    ;

    public boolean isEnabled() {
        return this == ENABLED;
    }

    public boolean isDisabledOrUnsupported() {
        return this != ENABLED;
    }

    public boolean isSupported() {
        return this != NOT_SUPPORTED;
    }
}
