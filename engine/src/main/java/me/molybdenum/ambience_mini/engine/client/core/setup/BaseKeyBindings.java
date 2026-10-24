package me.molybdenum.ambience_mini.engine.client.core.setup;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.client.core.providers.BaseGameStateProvider;


public abstract class BaseKeyBindings<TKeyBinding>
{
    @SuppressWarnings("rawtypes")
    private BaseClientCore core = null;

    public TKeyBinding openAmMenu;
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
        openAmMenu = createAndRegister(AmLang.KEY_OPEN_AM_MENU, keyEnd());
        printAllKey = createAndRegister(AmLang.KEY_PRINT_ALL, keyPageDown());
        toggleAreasKey = createAndRegister(AmLang.KEY_TOGGLE_AREAS, keyHome());
        areaConfirmKey = createAndRegister(AmLang.KEY_AREA_CONFIRM, keyInsert());
        areaCancelKey = createAndRegister(AmLang.KEY_AREA_CANCEL, keyDelete());
    }


    protected abstract int keyEnd();
    protected abstract int keyPageDown();
    protected abstract int keyHome();
    protected abstract int keyInsert();
    protected abstract int keyDelete();


    protected abstract TKeyBinding createAndRegister(AmLang description, int defaultKey);
    protected abstract boolean isClicked(TKeyBinding binding);

    public abstract String getKeyString(TKeyBinding binding);


    public void handleKeyInput()
    {
        if (isClicked(openAmMenu))
            core.openMainMenu();

        if (isClicked(printAllKey)) {
            BaseGameStateProvider provider = core.getGameStateProvider();
            core.notification.showToast(AmLang.MSG_PRINTING_ALL);

            var monitor = core.getMonitor();
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
}
