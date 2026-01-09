package me.molybdenum.ambience_mini.engine.core.setup;

import me.molybdenum.ambience_mini.engine.AmLang;
import me.molybdenum.ambience_mini.engine.core.BaseCore;
import me.molybdenum.ambience_mini.engine.core.providers.BaseGameStateProvider;


public abstract class BaseKeyBindings<TKeyBinding>
{
    @SuppressWarnings("rawtypes")
    private final BaseCore core;

    public TKeyBinding reloadKey;
    public TKeyBinding playPauseKey;
    public TKeyBinding nextMusicKey;
    public TKeyBinding printAll;


    @SuppressWarnings("rawtypes")
    public BaseKeyBindings(BaseCore core) {
        this.core = core;
    }


    public void registerKeys() {
        reloadKey = createAndRegister(AmLang.KEY_RELOAD, keyP());
        playPauseKey = createAndRegister(AmLang.KEY_PLAY_PAUSE, keyEnd());
        nextMusicKey = createAndRegister(AmLang.KEY_NEXT_MUSIC, keyPageUp());
        printAll = createAndRegister(AmLang.KEY_PRINT_ALL, keyPageDown());
    }


    protected abstract int keyP();
    protected abstract int keyEnd();
    protected abstract int keyPageUp();
    protected abstract int keyPageDown();


    protected abstract TKeyBinding createAndRegister(AmLang description, int defaultKey);
    protected abstract boolean isClicked(TKeyBinding binding);

    public abstract String getKeyString(TKeyBinding binding);
    public String getReloadKeyString() {
        return getKeyString(reloadKey);
    }


    public void handleKeyInput()
    {
        if (isClicked(reloadKey)) {
            core.notification.showToast(AmLang.MSG_RELOAD);
            core.tryReload();
        }

        if (isClicked(playPauseKey)) {
            var musicThread = core.getMusicThread();
            if (musicThread != null) {
                if (musicThread.isPaused()) {
                    core.notification.showToast(AmLang.MSG_RESUMING_MUSIC);
                    musicThread.play();
                } else {
                    core.notification.showToast(AmLang.MSG_PAUSING_MUSIC);
                    musicThread.pause();
                }
            }
        }

        if (isClicked(nextMusicKey)) {
            core.notification.showToast(AmLang.MSG_NEXT_MUSIC);

            var musicThread = core.getMusicThread();
            if (musicThread != null) {
                musicThread.forceSelectNewMusic();
                musicThread.play();
            }
        }

        if (isClicked(printAll) && core.levelState.notNull() && core.playerState.notNull()) {
            BaseGameStateProvider provider = core.getGameStateProvider();
            if (provider != null) {
                core.notification.showToast(AmLang.MSG_PRINTING_ALL);

                long timeStart = System.currentTimeMillis();
                String allState = provider.readAll();
                long elapsedTime = System.currentTimeMillis() - timeStart;

                core.logger.info("All current Ambience Mini state (took {} ms):\n{}", elapsedTime, allState);
            }
        }
    }
}
