package me.molybdenum.ambience_mini.state;

import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.state.Screens;
import me.molybdenum.ambience_mini.engine.state.StandardGameStateProvider;
import me.molybdenum.ambience_mini.handlers.ForgeEventHandlers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
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

public class GameStateProvider extends StandardGameStateProvider
{
    public static final String OBF_MAP_BOSS_INFO = "f_93699_";

    private final Minecraft mc = Minecraft.getInstance();

    private Vec3 _latestFishingPos = null;
    private long _latestFishingTime = 0L;


    public GameStateProvider() {
        super();
    }


    // ------------------------------------------------------------------------------------------------
    // Global events
    @Override
    public boolean inMainMenu() {
        if (mc.screen == null)
            ForgeEventHandlers.currentScreen = Screens.NONE;
        return ForgeEventHandlers.currentScreen == Screens.MAIN_MENU;
    }

    @Override
    public boolean isJoiningWorld() {
        if (mc.screen == null)
            ForgeEventHandlers.currentScreen = Screens.NONE;
        return ForgeEventHandlers.currentScreen == Screens.JOINING;
    }

    @Override
    public boolean isDisconnected() {
        if (mc.screen == null)
            ForgeEventHandlers.currentScreen = Screens.NONE;
        return ForgeEventHandlers.currentScreen == Screens.DISCONNECTED;
    }

    @Override
    public boolean isPaused() {
        if (mc.screen == null)
            ForgeEventHandlers.currentScreen = Screens.NONE;
        return ForgeEventHandlers.currentScreen == Screens.PAUSE;
    }

    @Override
    public boolean onCreditsScreen() {
        if (mc.screen == null)
            ForgeEventHandlers.currentScreen = Screens.NONE;
        return ForgeEventHandlers.currentScreen == Screens.CREDITS;
    }

    @Override
    public boolean inGame() {
        return mc.level != null;
    }


    // ------------------------------------------------------------------------------------------------
    // Time-based events
    @Override
    public boolean isDay() {
        int time = this.getTime();            // "12542" is the time when beds can be used.
        return time > 23500 || time <= 12500; // "23460" is the time from when beds cannot be used.
    }

    @Override
    public boolean isDawn() {
        int time = this.getTime();
        return time > 23500 || time <= 2000;
    }

    @Override
    public boolean isDusk() {
        int time = this.getTime();
        return time > 10300 && time <= 12500;
    }

    @Override
    public boolean isNight() {
        int time = this.getTime();
        return time > 12500 && time <= 23500;
    }


    // ------------------------------------------------------------------------------------------------
    // Weather-based events
    @Override
    public boolean isDownfall() {
        return mc.level != null && mc.level.isRaining();
    }

    @Override
    public boolean isRaining() {
        return isDownfall() && !isColdEnoughToSnow();
    }

    @Override
    public boolean isSnowing() {
        return isDownfall() && isColdEnoughToSnow();
    }

    @Override
    public boolean isThundering() {
        return mc.level != null && mc.level.isThundering();
    }


    // ------------------------------------------------------------------------------------------------
    // Location-based events
    @Override
    public boolean inVillage() {
        if (mc.player == null || mc.level == null)
            return false;

        var playerPos = mc.player.blockPosition();
        var area = new AABB(playerPos.getX() - 30, playerPos.getY() - 15, playerPos.getZ() - 30, playerPos.getX() + 30, playerPos.getY() + 15, playerPos.getZ() + 30);

        var nearbyVillagerCount = mc.level.getEntitiesOfClass(Villager.class, area, ignore -> true).size();
        return nearbyVillagerCount >= Common.IN_VILLAGE_THRESHOLD;
    }

    @Override
    public boolean inRanch() {
        if (mc.player == null || mc.level == null)
            return false;

        var playerPos = mc.player.blockPosition();
        var area = new AABB(playerPos.getX() - 30, playerPos.getY() - 8, playerPos.getZ() - 30, playerPos.getX() + 30, playerPos.getY() + 8, playerPos.getZ() + 30);

        var nearbyAnimalsCount = mc.level.getEntitiesOfClass(Animal.class, area, ignore -> true).size();
        return nearbyAnimalsCount >= Common.IN_RANCH_THRESHOLD;
    }

    @Override
    public boolean isUnderDeepslate() {
        return getPlayerElevation() < 0;
    }

    @Override
    public boolean isUnderground() {
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

    @Override
    public boolean isHighUp() {
        return getPlayerElevation() > Common.HIGH_UP_THRESHOLD;
    }

    @Override
    public boolean isUnderWater() {
        return mc.player != null && mc.player.isUnderWater();
    }

    @Override
    public boolean inLava() {
        return mc.player != null && mc.player.isInLava();
    }


    // ------------------------------------------------------------------------------------------------
    // Player-state-based events
    @Override
    public boolean isDead() {
        return mc.screen instanceof DeathScreen;
    }

    @Override
    public boolean isSleeping() {
        return mc.player != null && mc.player.isSleeping();
    }

    @Override
    public boolean isFishing() {
        if (mc.player == null || mc.level == null)
            return false;

        if (mc.player.fishing != null && mc.player.fishing.isInWater()) { // If player is fishing
            _latestFishingPos = mc.player.position();
            _latestFishingTime = System.currentTimeMillis();
        }
        else if (_latestFishingPos != null) { // Grace period where "isFishing" will not turn off to prevent "shuffling" music back and forth.
            if (_latestFishingPos.subtract(mc.player.position()).length() > 1f || System.currentTimeMillis() - _latestFishingTime > Common.FISHING_TIMEOUT_MILLIS)
                _latestFishingPos = null;
        }

        return _latestFishingPos != null;
    }


    // ------------------------------------------------------------------------------------------------
    // Mount events
    @Override
    public boolean inMinecart() {
        if (mc.player == null)
            return false;
        return mc.player.getVehicle() instanceof Minecart;
    }

    @Override
    public boolean inBoat() {
        if (mc.player == null)
            return false;
        return mc.player.getVehicle() instanceof Boat;
    }

    @Override
    public boolean onHorse() {
        if (mc.player == null)
            return false;
        return mc.player.getVehicle() instanceof Horse;
    }

    @Override
    public boolean onDonkey() {
        if (mc.player == null)
            return false;
        return mc.player.getVehicle() instanceof Donkey;
    }

    @Override
    public boolean onPig() {
        if (mc.player == null)
            return false;
        return mc.player.getVehicle() instanceof Pig;
    }

    @Override
    public boolean flyingElytra() {
        if (mc.player == null)
            return false;
        return mc.player.isFallFlying();
    }


    // ------------------------------------------------------------------------------------------------
    // Combat events
    @Override
    public boolean inCombat() {
        return false;    // TODO : Make actual implementation
    }

    @Override
    public boolean inBossFight() {
        return !getBossId().isEmpty();
    }



    // ------------------------------------------------------------------------------------------------
    // Properties
    @Override
    public String getDimensionId() {
        return mc.level != null ? mc.level.dimension().location().toString() : "";
    }

    @Override
    public String getBiomeId() {
        if (mc.level == null || mc.player == null)
            return "";
        return getBiomeId(mc.level.getBiome(mc.player.blockPosition()));
    }

    @Override
    public String getVehicleId() {
        if (mc.player == null || mc.player.getVehicle() == null)
            return "";
        return mc.player.getVehicle().getEncodeId();
    }

    @Override
    public String getBossId() {
        var bossOverlay = mc.gui.getBossOverlay();
        Map<UUID, LerpingBossEvent> bossMap = ObfuscationReflectionHelper.getPrivateValue(BossHealthOverlay.class, bossOverlay, OBF_MAP_BOSS_INFO);
        if (bossMap == null || bossMap.isEmpty())
            return "";

        var bossEvent = bossMap.values()
                .stream()
                .findFirst()
                .get();
        return ((TranslatableContents)bossEvent.getName().getContents()).getKey();
    }

    @Override
    public int getTime() {
        return (int) (mc.level != null ? mc.level.getDayTime() : 0) % 24000;
    }

    @Override
    public float getPlayerElevation() {
        return (float)(mc.player != null ? mc.player.position().y : 0f);
    }

    public float getPlayerHealth() {
        return (mc.player != null ? mc.player.getHealth() : 0f);
    }


    // ------------------------------------------------------------------------------------------------
    // Utilities
    private boolean isColdEnoughToSnow() {
        if (mc.level == null || mc.player == null)
            return false;
        BlockPos blockPos = mc.player.blockPosition();
        return mc.level.getBiome(blockPos).get().coldEnoughToSnow(blockPos);
    }

    private static String getBiomeId(Holder<Biome> biomeHolder) {
        return biomeHolder.unwrap().map(
                (resourceKey) -> resourceKey.location().toString(),
                (biome) -> ""
        );
    }
}
