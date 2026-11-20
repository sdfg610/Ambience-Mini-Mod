package me.molybdenum.ambience_mini.engine.client.core.setup;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.music.MusicEngine;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;


public abstract class BaseKeyBindings<TKeyBinding>
{
    @SuppressWarnings("rawtypes")
    private BaseClientCore core = null;

    public TKeyBinding openAmMenuKey;

    public TKeyBinding reloadKey;
    public TKeyBinding playPauseKey;
    public TKeyBinding nextMusicKey;

    public TKeyBinding printAllKey;

    public TKeyBinding toggleAreasKey;
    public TKeyBinding areaConfirmKey;
    public TKeyBinding areaCancelKey;


    @SuppressWarnings("rawtypes")
    public void init(BaseClientCore core) {
        if (this.core != null)
            throw new RuntimeException("Multiple calls to 'BaseKeyBindings.init'!");
        this.core = core;
    }


    public void registerKeys() {
        openAmMenuKey = createAndRegister(AmLang.KEY_OPEN_AM_MENU, keyEnd());

        reloadKey = createAndRegister(AmLang.KEY_RELOAD, keyNone());
        playPauseKey = createAndRegister(AmLang.KEY_PLAY_PAUSE, keyNone());
        nextMusicKey = createAndRegister(AmLang.KEY_NEXT_MUSIC, keyNone());

        printAllKey = createAndRegister(AmLang.KEY_PRINT_ALL, keyPageDown());

        toggleAreasKey = createAndRegister(AmLang.KEY_TOGGLE_AREAS, keyHome());
        areaConfirmKey = createAndRegister(AmLang.KEY_AREA_CONFIRM, keyInsert());
        areaCancelKey = createAndRegister(AmLang.KEY_AREA_CANCEL, keyDelete());
    }


    protected abstract int keyEnd();
    protected abstract int keyNone();
    protected abstract int keyPageDown();
    protected abstract int keyHome();
    protected abstract int keyInsert();
    protected abstract int keyDelete();


    protected abstract TKeyBinding createAndRegister(AmLang description, int defaultKey);
    protected abstract boolean isClicked(TKeyBinding binding);

    public abstract String getKeyString(TKeyBinding binding);


    public String getMenuKeyString() {
        return getKeyString(openAmMenuKey);
    }


    public void handleKeyInput()
    {
        if (isClicked(openAmMenuKey))
            core.openMainMenu();

        if (isClicked(reloadKey)) {
            core.notification.showToast(AmLang.MSG_RELOAD);
            MusicEngine.enableAutoRestart();
            core.tryReloadMusicEngine();
        }

        if (isClicked(playPauseKey) && checkMonitorRunning()) {
            var musicEngine = core.getMusicEngine();
            if (musicEngine.isPaused()) {
                core.notification.showToast(AmLang.MSG_RESUMING_MUSIC);
                musicEngine.resume();
            } else {
                core.notification.showToast(AmLang.MSG_PAUSING_MUSIC);
                musicEngine.pause();
            }
        }

        if (isClicked(nextMusicKey) && checkMonitorRunning()) {
            if (!core.clientConfig.printNowPlaying.get())
                core.notification.showToast(AmLang.MSG_NEXT_MUSIC);

            var musicEngine = core.getMusicEngine();
            musicEngine.forceSelectNewMusic();
            musicEngine.resume();
        }

        if (isClicked(printAllKey)) {
            BaseGameStateProvider provider = core.getGameStateProvider();
            core.notification.showToast(AmLang.MSG_PRINTING_ALL);

            var monitor = core.getMusicEngine();
            if (monitor == null || !monitor.isRunning())
                provider.prepare(null);

            long timeStart = System.currentTimeMillis();
            String allState = provider.readAll();
            long elapsedTime = System.currentTimeMillis() - timeStart;

            core.logger.info("All current Ambience Mini state (took {} ms):\n{}", elapsedTime, allState);
        }

        if (isClicked(toggleAreasKey))
            switch (core.areaRenderer.setOrToggleViewMode(null)) {
                case OFF -> core.notification.showToast(AmLang.MSG_AREA_VIEW_OFF);
                case AREA_SELECTION -> core.notification.showToast(AmLang.MSG_AREA_SELECTOR_ENABLED);
                case AREA_CONSTRUCTION -> core.notification.showToast(AmLang.MSG_AREA_CONSTRUCTOR_ENABLED);
            }

        if (isClicked(areaConfirmKey))
            core.areaRenderer.registerConfirm();

        if (isClicked(areaCancelKey))
            core.areaRenderer.registerCancel();
    }

    private boolean checkMonitorRunning() {
        if (core.isEngineRunning())
            return true;
        core.notification.showToast(AmLang.MSG_NOT_RUNNING);
        return false;
    }
}
