package me.molybdenum.ambience_mini.engine.setup;

public abstract class BaseKeyBindings<KeyBinding> {
    public KeyBinding reloadKey;
    public KeyBinding nextMusicKey;
    public KeyBinding showCaveScore;

    public BaseKeyBindings<KeyBinding> registerKeys() {
        reloadKey = makeAndRegister("key.reload", keyP());
        nextMusicKey = makeAndRegister("key.nextMusic", keyPageUp());
        showCaveScore = makeAndRegister("key.showCaveScore", keyPageDown());
        return this;
    }

    protected abstract KeyBinding makeAndRegister(String description, int defaultKey);

    protected abstract int keyP();
    protected abstract int keyPageUp();
    protected abstract int keyPageDown();
}
