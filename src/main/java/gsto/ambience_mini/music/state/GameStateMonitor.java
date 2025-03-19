package gsto.ambience_mini.music.state;

import gsto.ambience_mini.AmbienceMini;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
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
    public static final long FISHING_TIMEOUT_MILLIS = 4000;

    public static final long HIGH_UP_THRESHOLD = 150;
    public static final long IN_VILLAGE_THRESHOLD = 3;
    public static final long IN_RANCH_THRESHOLD = 15;

    private static Minecraft mc = null;

    // Running state
    private static Vec3 _latestFishingPos = null;
    private static long _latestFishingTime = 0L;

    private static Screens currentScreen = Screens.NONE;


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
        if (mc.screen == null)
            currentScreen = Screens.NONE;
        return currentScreen == Screens.MAIN_MENU;
    }

    public static boolean isJoiningWorld() {
        if (mc.screen == null)
            currentScreen = Screens.NONE;
        return currentScreen == Screens.JOINING;
    }

    public static boolean isDisconnected() {
        if (mc.screen == null)
            currentScreen = Screens.NONE;
        return currentScreen == Screens.DISCONNECTED;
    }

    public static boolean isPaused() {
        if (mc.screen == null)
            currentScreen = Screens.NONE;
        return currentScreen == Screens.PAUSE;
    }

    public static boolean onCreditsScreen() {
        if (mc.screen == null)
            currentScreen = Screens.NONE;
        return currentScreen == Screens.CREDITS;
    }

    public static boolean inGame() {
        return mc.level != null;
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

        if (mc.player.fishing != null && mc.player.fishing.isInWater()) { // If player is fishing
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
        return nearbyVillagerCount >= IN_VILLAGE_THRESHOLD;
    }

    public static boolean inRanch() {
        if (mc.player == null || mc.level == null)
            return false;

        var playerPos = mc.player.blockPosition();
        var area = new AABB(playerPos.getX() - 30, playerPos.getY() - 8, playerPos.getZ() - 30, playerPos.getX() + 30, playerPos.getY() + 8, playerPos.getZ() + 30);

        var nearbyAnimalsCount = mc.level.getEntitiesOfClass(Animal.class, area, ignore -> true).size();
        return nearbyAnimalsCount >= IN_RANCH_THRESHOLD;
    }


    // ------------------------------------------------------------------------------------------------
    // Mounts
    public static boolean inMinecart() {
        if (mc.player == null)
            return false;
        return mc.player.getVehicle() instanceof Minecart;
    }

    public static boolean inBoat() {
        if (mc.player == null)
            return false;
        return mc.player.getVehicle() instanceof Boat;
    }

    public static boolean onHorse() {
        if (mc.player == null)
            return false;
        return mc.player.getVehicle() instanceof Horse;
    }

    public static boolean onDonkey() {
        if (mc.player == null)
            return false;
        return mc.player.getVehicle() instanceof Donkey;
    }

    public static boolean onPig() {
        if (mc.player == null)
            return false;
        return mc.player.getVehicle() instanceof Pig;
    }

    public static String getVehicle() {
        if (mc.player == null || mc.player.getVehicle() == null)
            return "";
        return mc.player.getVehicle().getEncodeId();
    }


    // ------------------------------------------------------------------------------------------------
    // Time- and weather-based
    public static int getTime() {
        return (int) (mc.level != null ? mc.level.getDayTime() : 0) % 24000;
    }

    public static boolean isRaining() {
        return mc.level != null && mc.level.isRaining();
    }

    public static boolean isThundering() {
        return mc.level != null && mc.level.isThundering();
    }

    public static boolean isColdEnoughToSnow() {
        if (mc.level == null || mc.player == null)
            return false;
        BlockPos blockPos = mc.player.blockPosition();
        return mc.level.getBiome(blockPos).get().coldEnoughToSnow(blockPos);
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
            currentScreen = Screens.JOINING;
        else if (screen instanceof DisconnectedScreen || screen instanceof DisconnectedRealmsScreen)
            currentScreen = Screens.DISCONNECTED;
        else if (screen instanceof PauseScreen)
            currentScreen = Screens.PAUSE;
        else if (screen instanceof WinScreen)
            currentScreen = Screens.CREDITS;
        else if (screen instanceof TitleScreen || screen instanceof JoinMultiplayerScreen || screen instanceof DirectJoinServerScreen || screen instanceof SelectWorldScreen) {
            currentScreen = Screens.MAIN_MENU;
        }
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
