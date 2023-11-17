package gsto.ambience_mini.state;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class PlayerStateChecker
{
    public static String currentDimensionName(LocalPlayer player)
    {
        return player.level.dimension().location().toString();
    }

    public static boolean isSleeping(LocalPlayer player)
    {
        return player.isSleeping();
    }

    public static boolean isDead(LocalPlayer player)
    {
        return player.isDeadOrDying();
    }



    public static boolean inBossFight()
    {
        // TODO: DO BETTER
        return Minecraft.getInstance().gui.getBossOverlay().shouldPlayMusic();
    }
}
