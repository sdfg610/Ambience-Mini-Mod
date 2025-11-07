package me.molybdenum.ambience_mini.engine.setup;

import me.molybdenum.ambience_mini.engine.AmLang;

public abstract class BaseKeyBindings<TKeyBinding> {
    public TKeyBinding reloadKey;
    public TKeyBinding nextMusicKey;
    public TKeyBinding showCaveScore;

    public void registerKeys() {
        reloadKey = createAndRegister(AmLang.KEY_RELOAD, keyP());
        nextMusicKey = createAndRegister(AmLang.KEY_NEXT_MUSIC, keyPageUp());
        showCaveScore = createAndRegister(AmLang.KEY_SHOW_CAVE_SCORE, keyPageDown());
    }

    protected abstract TKeyBinding createAndRegister(AmLang description, int defaultKey);

    protected abstract int keyP();
    protected abstract int keyPageUp();
    protected abstract int keyPageDown();
}
