package me.molybdenum.ambience_mini.engine.state.monitors;

import java.util.Optional;

public abstract class BaseScreenMonitor
{
    public Screens memorizedScreen = Screens.NONE;

    public abstract boolean isScreenNull();

    public abstract boolean isDeathScreen();

    public abstract Optional<String> getBossIdIfInFight();
}
