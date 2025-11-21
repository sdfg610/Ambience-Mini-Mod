package me.molybdenum.ambience_mini.engine.core.state;

public abstract class BaseScreenState
{
    private Screens memorizedScreen = Screens.NONE;


    public Screens getMemorizedScreen() {
        if (isCurrentScreenNull())
            return memorizedScreen = Screens.NONE;
        return memorizedScreen;
    }
    
    public void setMemorizedScreen(Screens screen) {
        memorizedScreen = screen;
    }

    public boolean is(Screens screen) {
        return getMemorizedScreen() == screen;
    }


    protected abstract boolean isCurrentScreenNull();
}
