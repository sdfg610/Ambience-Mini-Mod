package me.molybdenum.ambience_mini.engine.core.setup;

import me.molybdenum.ambience_mini.engine.AmLang;
import me.molybdenum.ambience_mini.engine.core.BaseCore;


public abstract class BaseKeyBindings<TKeyBinding>
{
    @SuppressWarnings("rawtypes")
    private final BaseCore core;

    public TKeyBinding reloadKey;
    public TKeyBinding nextMusicKey;
    public TKeyBinding printAll;


    @SuppressWarnings("rawtypes")
    public BaseKeyBindings(BaseCore core) {
        this.core = core;
    }


    public void registerKeys() {
        reloadKey = createAndRegister(AmLang.KEY_RELOAD, keyP());
        nextMusicKey = createAndRegister(AmLang.KEY_NEXT_MUSIC, keyPageUp());
        printAll = createAndRegister(AmLang.KEY_PRINT_ALL, keyPageDown());
    }


    protected abstract int keyP();
    protected abstract int keyPageUp();
    protected abstract int keyPageDown();


    protected abstract TKeyBinding createAndRegister(AmLang description, int defaultKey);
    protected abstract boolean isClicked(TKeyBinding binding);


    public void handleKeyInput()
    {
        if (isClicked(reloadKey)) {
            core.notification.showToast(AmLang.TOAST_RELOAD);
            core.tryReload();
        }

        if (isClicked(nextMusicKey)) {
            core.notification.showToast(AmLang.TOAST_NEXT_MUSIC);

            var musicThread = core.getMusicThread();
            if (musicThread != null)
                musicThread.forceSelectNewMusic();
        }

        if (isClicked(printAll) && core.levelState.notNull() && core.playerState.notNull()) {
            core.notification.showToast(AmLang.TOAST_PRINTING_ALL);
            core.logger.info("All current Ambience Mini state:\n{}", core.getGameStateProvider().readAll());
        }
    }
}
