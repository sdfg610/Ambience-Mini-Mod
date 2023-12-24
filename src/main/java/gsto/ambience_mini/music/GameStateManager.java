package gsto.ambience_mini.music;

import gsto.ambience_mini.AmbienceMini;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.Map;
import java.util.UUID;

public class GameStateManager
{
    private static Minecraft mc = null;
    private static boolean isJoiningWorld = false;
    private static boolean isPaused = false;


    public static void init()
    {
        mc = Minecraft.getInstance();
    }


    //
    // Menu states
    //
    public static boolean inMainMenu()
    {
        return mc.player == null || mc.level == null;
    }

    public static boolean inGame()
    {
        return mc.player != null && mc.level != null;
    }

    public static boolean inPauseMenu()
    {
        return isPaused;
    }

    public static boolean possiblyInSoundOptions()
    {
        return isPaused || inMainMenu();
    }


    public static boolean isJoiningWorld()
    {
        return isJoiningWorld;
    }

    public static boolean onDisconnectedScreen()
    {
        return mc.screen instanceof DisconnectedScreen;
    }

    public static boolean onCreditsScreen()
    {
        return mc.screen instanceof WinScreen;
    }


    public static boolean isGameFocused()
    {
        return mc.isWindowActive();
    }


    //
    // Environmental states
    //
    public static boolean isNight()
    {
        assert mc.level != null;
        assert mc.player != null;

        long time = mc.level.getDayTime() % 24000;
        return time > 13200 && time < 23200;
    }

    public static boolean isDownfall()
    {
        assert mc.level != null;
        assert mc.player != null;
        return mc.level.isRaining();
    }


    //
    // Player states
    //
    public static int getPlayerElevation()
    {
        assert mc.player != null;
        return mc.player.blockPosition().getY();
    }

    public static boolean isSleeping()
    {
        assert mc.player != null;
        return mc.player.isSleeping();
    }

    public static boolean isFishing()
    {
        assert mc.player != null;
        return mc.player.fishing != null;
    }

    public static boolean isDead()
    {
        assert mc.player != null;
        return mc.player.isDeadOrDying();
    }

    public static boolean isUnderground()
    {
        assert mc.player != null;
        assert mc.level != null;

        var playerPos = mc.player.blockPosition();
        var level = mc.level;

        if (level.getBiome(playerPos).is(Tags.Biomes.IS_UNDERGROUND))
            return true;

        return playerPos.getY() <= 60
                && !level.canSeeSky(playerPos)
                && level.getBrightness(LightLayer.SKY, playerPos) - level.getSkyDarken() <= 0;
    }

    public static boolean isUnderWater()
    {
        assert mc.player != null;
        return mc.player.isUnderWater();
    }

    public static boolean isInVillage()
    {
        assert mc.player != null;
        assert mc.level != null;

        var playerPos = mc.player.blockPosition();
        var area = new AABB(playerPos.getX() - 30, playerPos.getY() - 8, playerPos.getZ() - 30, playerPos.getX() + 30, playerPos.getY() + 8, playerPos.getZ() + 30);
        var nearbyVillagerCount = mc.level.getEntitiesOfClass(Villager.class, area, ignore -> true).size();

        return nearbyVillagerCount >= 2;
    }


    public static String getDimensionId()
    {
        assert mc.level != null;
        return mc.level.dimension().location().toString();
    }

    public static String getBossId()
    {
        var bossOverlay = mc.gui.getBossOverlay();
        Map<UUID, LerpingBossEvent> bossMap = ObfuscationReflectionHelper.getPrivateValue(BossHealthOverlay.class, bossOverlay, AmbienceMini.OBF_MAP_BOSS_INFO);
        if (bossMap == null || bossMap.isEmpty())
            return null;

        var bossEvent = bossMap.values().stream().findFirst().get();
        return ((TranslatableComponent)bossEvent.getName()).getKey();
    }


    //
    // Handle events
    //

    public static void handleScreen(Screen screen)
    {
        if (screen == null || screen instanceof TitleScreen)
        {
            isJoiningWorld = false;
            isPaused = false;
        }
        else if (screen instanceof LevelLoadingScreen || screen instanceof ConnectScreen || screen instanceof ReceivingLevelScreen || screen instanceof GenericDirtMessageScreen)
            isJoiningWorld = true;
        else if (screen instanceof PauseScreen)
            isPaused = true;
    }
}
