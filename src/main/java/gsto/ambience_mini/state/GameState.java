package gsto.ambience_mini.state;

import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public class GameState
{
    @Nullable public static String currentScreen = null;
    @Nullable public static String currentDimension = null;
    @Nullable public static String currentEvent = null;
    @Nullable public static String currentBiome = null;
    @Nullable public static String currentMobs = null;

    public static boolean isAttacked = false;






    public void HandleScreen(Screen screen)
    {

    }
}
