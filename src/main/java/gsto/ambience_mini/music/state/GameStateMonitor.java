package gsto.ambience_mini.music.state;

import gsto.ambience_mini.AmbienceMini;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.Map;
import java.util.UUID;

public class GameStateMonitor
{
    public static final long HIGH_UP_THRESHOLD = 150;
    public static final long FISHING_TIMEOUT_MILLIS = 4000;

    private static Minecraft mc = null;

    // Running state
    private static Vec3 _latestFishingPos = null;
    private static long _latestFishingTime = 0L;

    private static boolean possiblyJoiningWorld = false;


    public static void init() {
        mc = Minecraft.getInstance();
    }


    // ------------------------------------------------------------------------------------------------
    // Meta states
    public static boolean isGameFocused() {
        return mc.isWindowActive();
    }


    // ------------------------------------------------------------------------------------------------
    // Global states
    public static boolean inMainMenu() {
        return mc.player == null && mc.level == null;
    }

    public static boolean isJoiningWorld() {
        if (mc.screen == null) // mc.player != null ||
            possiblyJoiningWorld = false;
        return possiblyJoiningWorld;
    }

    public static boolean inGame() {
        return mc.level != null;
    }

    public static boolean onCreditsScreen() {
        return mc.screen instanceof WinScreen;
    }


    // ------------------------------------------------------------------------------------------------
    // Player- and Location-based
    public static String getDimensionId() {
        return mc.level != null ? mc.level.dimension().location().toString() : "";
    }

    public static String getBiomeId() {
        if (mc.level == null || mc.player == null)
            return "";
        return printBiome(mc.level.getBiome(mc.player.blockPosition()));
    }

    public static float getPlayerElevation() {
        return (float) (mc.player != null ? mc.player.position().y : 0f);
    }

    public static boolean isSleeping() {
        return mc.player != null && mc.player.isSleeping();
    }

    public static boolean isFishing() {
        if (mc.player == null || mc.level == null)
            return false;

        if (mc.player.fishing != null) { // If player is fishing
            _latestFishingPos = mc.player.position();
            _latestFishingTime = System.currentTimeMillis();
        }
        else if (_latestFishingPos != null) { // Grace period where "isFishing" will not turn off to prevent "shuffling" music back and forth.
            if (_latestFishingPos.subtract(mc.player.position()).length() > 1f || System.currentTimeMillis() - _latestFishingTime > FISHING_TIMEOUT_MILLIS)
                _latestFishingPos = null;
        }

        return _latestFishingPos != null;
    }

    public static boolean isDead() {
        return mc.screen instanceof DeathScreen;
    }


    public static boolean isUnderground() {
        if (mc.player == null || mc.level == null)
            return false;

        var playerPos = mc.player.blockPosition();
        var level = mc.level;

        if (level.getBiome(playerPos).is(Tags.Biomes.IS_UNDERGROUND))
            return true;

        return playerPos.getY() <= 60
                && !level.canSeeSky(playerPos)
                && level.getBrightness(LightLayer.SKY, playerPos) - level.getSkyDarken() <= 0;
    }

    public static boolean isUnderWater() {
        return mc.player != null && mc.player.isUnderWater();
    }

    public static boolean inVillage() {
        if (mc.player == null || mc.level == null)
            return false;

        var playerPos = mc.player.blockPosition();
        var area = new AABB(playerPos.getX() - 30, playerPos.getY() - 15, playerPos.getZ() - 30, playerPos.getX() + 30, playerPos.getY() + 15, playerPos.getZ() + 30);
        var nearbyVillagerCount = mc.level.getEntitiesOfClass(Villager.class, area, ignore -> true).size();

        return nearbyVillagerCount >= 2;
    }


    // ------------------------------------------------------------------------------------------------
    // Time- and weather-based
    public static int getTime() {
        return (int) (mc.level != null ? mc.level.getDayTime() : 0) % 24000;
    }

    public static float getRainLevel() {
        return mc.level != null ? mc.level.getRainLevel(1f) : 0f;
    }

    public static boolean isThundering() {
        return mc.level != null && mc.level.isThundering();
    }


    // ------------------------------------------------------------------------------------------------
    // Combat
    public static String getBossId()
    {
        var bossOverlay = mc.gui.getBossOverlay();
        Map<UUID, LerpingBossEvent> bossMap = ObfuscationReflectionHelper.getPrivateValue(BossHealthOverlay.class, bossOverlay, AmbienceMini.OBF_MAP_BOSS_INFO);
        if (bossMap == null || bossMap.isEmpty())
            return "";

        var bossEvent = bossMap.values()
                .stream()
                .findFirst()
                .get();
        return ((TranslatableContents)bossEvent.getName().getContents()).getKey();
    }


    // ------------------------------------------------------------------------------------------------
    // Handlers
    public static void handleScreen(Screen screen) {
        if (screen instanceof GenericDirtMessageScreen || screen instanceof ConnectScreen)
            possiblyJoiningWorld = true;
        else if (screen instanceof DisconnectedScreen || screen instanceof TitleScreen)
            possiblyJoiningWorld = false;
    }


    // ------------------------------------------------------------------------------------------------
    // Helpers
    private static String printBiome(Holder<Biome> biomeHolder) {
        return biomeHolder.unwrap().map(
                (resourceKey) -> resourceKey.location().toString(),
                (biome) -> ""
        );
    }
}
