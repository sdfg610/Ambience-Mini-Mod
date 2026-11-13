package me.molybdenum.ambience_mini.engine.client.core.state;

import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.McScreen;
import me.molybdenum.ambience_mini.engine.client.core.gui.menus.AreaMenu;
import me.molybdenum.ambience_mini.engine.client.core.gui.menus.MainMenu;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseClientConfig;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;


public abstract class BaseScreenState
{
    private static final Map<Class<?>, ScreenType> SCREEN_CLASS_TO_TYPE = new HashMap<>();

    private boolean loaded;
    private BaseClientConfig clientConfig;
    private Logger logger;

    private ScreenType memorizedScreenType = ScreenType.NONE;
    private String currentScreenID = null;


    @SuppressWarnings("rawtypes")
    public void init(
            BaseClientCore core
    ) {
        if (loaded)
            throw new RuntimeException("Multiple calls to 'BaseCombatState.init'!");

        clientConfig = core.clientConfig;
        logger = core.logger;

        loaded = true;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    protected abstract boolean isScreenNull();


    // -----------------------------------------------------------------------------------------------------------------
    // Screen operations
    public ScreenType getMemorizedScreenType() {
        if (isScreenNull()) // Not all Minecraft versions fire "screen changed" for null-screen.
            return memorizedScreenType = ScreenType.NONE;
        return memorizedScreenType;
    }

    public String getCurrentScreenID() {
        if (isScreenNull()) // Not all Minecraft versions fire "screen changed" for null-screen.
            return currentScreenID = null;
        return currentScreenID;
    }


    public void handleScreenChanged(Object screen) {
        if (screen == null) {
            memorizedScreenType = ScreenType.NONE;
            currentScreenID = null;
        }
        else {
            var clazz = screen instanceof McScreen mcScreen ? mcScreen.getAmMenu().getClass() : screen.getClass();
            var newType = getScreenTypeOfClass(clazz);
            if (newType != ScreenType.UNKNOWN)
                memorizedScreenType = newType;
            currentScreenID = clazz.getCanonicalName();
        }

        if (loaded && clientConfig.printScreenOnChange.get())
            logger.info("Changed to menu with ID '{}' and type '{}'", currentScreenID, memorizedScreenType.name);
    }

    private ScreenType getScreenTypeOfClass(Class<?> screenClass) {
        return SCREEN_CLASS_TO_TYPE.computeIfAbsent(screenClass, clazz -> {
                    if (AreaMenu.class == screenClass)
                        return ScreenType.AREA_MENU;
                    else if (MainMenu.class == screenClass)
                        return ScreenType.AM_MENU;

                    return switch (clazz.getCanonicalName()) {
                        case "net.minecraft.client.gui.screens.TitleScreen",
                             "net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen",
                             "net.minecraft.client.gui.screens.DirectJoinServerScreen",
                             "net.minecraft.client.gui.screens.worldselection.SelectWorldScreen",
                             "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen" -> ScreenType.MAIN_MENU;

                        case "net.minecraft.client.gui.screens.ProgressScreen",
                             "net.minecraft.client.gui.screens.ConnectScreen",
                             "net.minecraft.client.gui.screens.LevelLoadingScreen",
                             "net.minecraft.client.gui.screens.ReceivingLevelScreen" -> ScreenType.JOINING;

                        case "net.minecraft.client.gui.screens.DisconnectedScreen",
                             "net.minecraft.realms.DisconnectedRealmsScreen" -> ScreenType.DISCONNECTED;

                        case "net.minecraft.client.gui.screens.WinScreen" -> ScreenType.CREDITS;

                        case "net.minecraft.client.gui.screens.DeathScreen" -> ScreenType.DEATH;

                        case "net.minecraft.client.gui.screens.PauseScreen" -> ScreenType.PAUSE;

                        case "net.minecraft.client.gui.screens.inventory.InventoryScreen",
                             "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen" ->
                                ScreenType.PLAYER_INVENTORY;

                        default -> ScreenType.UNKNOWN;
                    };
                }
        );
    }
}
