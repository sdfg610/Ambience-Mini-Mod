package me.molybdenum.ambience_mini.engine.client.core.setup;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;


public abstract class BaseKeyBindings<TKeyBinding>
{
    @SuppressWarnings("rawtypes")
    private BaseClientCore core = null;

    public TKeyBinding reloadKey;
    public TKeyBinding playPauseKey;
    public TKeyBinding nextMusicKey;
    public TKeyBinding printAll;
    public TKeyBinding toggleAreas;
    public TKeyBinding areaConfirm;
    public TKeyBinding areaCancel;


    @SuppressWarnings("rawtypes")
    public void init(BaseClientCore core) {
        if (this.core != null)
            throw new RuntimeException("Multiple calls to 'BaseKeyBindings.init'!");
        this.core = core;
    }


    public void registerKeys() {
        reloadKey = createAndRegister(AmLang.KEY_RELOAD, keyP());
        playPauseKey = createAndRegister(AmLang.KEY_PLAY_PAUSE, keyEnd());
        nextMusicKey = createAndRegister(AmLang.KEY_NEXT_MUSIC, keyPageUp());
        printAll = createAndRegister(AmLang.KEY_PRINT_ALL, keyPageDown());
        toggleAreas = createAndRegister(AmLang.KEY_TOGGLE_AREAS, keyHome());
        areaConfirm = createAndRegister(AmLang.KEY_AREA_CONFIRM, keyInsert());
        areaCancel = createAndRegister(AmLang.KEY_AREA_CANCEL, keyDelete());
    }


    protected abstract int keyP();
    protected abstract int keyEnd();
    protected abstract int keyPageUp();
    protected abstract int keyPageDown();
    protected abstract int keyHome();
    protected abstract int keyInsert();
    protected abstract int keyDelete();


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
            core.tryReloadMusicEngine();
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

        if (isClicked(toggleAreas))
            switch (core.areaRenderer.setOrToggleViewMode(null)) {
                case OFF -> core.notification.showToast(AmLang.MSG_AREA_VIEW_OFF);
                case AREA_SELECTION -> core.notification.showToast(AmLang.MSG_AREA_SELECTOR_ENABLED);
                case AREA_CONSTRUCTION -> core.notification.showToast(AmLang.MSG_AREA_CONSTRUCTOR_ENABLED);
            }

        if (isClicked(areaConfirm))
            core.areaRenderer.registerConfirm();

        if (isClicked(areaCancel))
            core.areaRenderer.registerCancel();
    }
}
