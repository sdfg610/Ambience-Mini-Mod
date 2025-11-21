package me.molybdenum.ambience_mini.engine.core.state;

import java.util.List;

public abstract class BaseScreenState
{
    private Screens memorizedScreen = Screens.NONE;


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    protected abstract boolean isScreenNull();


    // -----------------------------------------------------------------------------------------------------------------
    // Screen operations
    public Screens getMemorizedScreen() {
        if (isScreenNull())
            return memorizedScreen = Screens.NONE;
        return memorizedScreen;
    }

    public void setMemorizedScreen(Screens screen) {
        memorizedScreen = screen;
    }
    public boolean is(Screens screen) {
        return getMemorizedScreen() == screen;
    }
}
