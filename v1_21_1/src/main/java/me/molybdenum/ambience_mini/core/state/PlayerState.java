package me.molybdenum.ambience_mini.core.state;

import me.molybdenum.ambience_mini.engine.core.state.BasePlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.extensions.IHolderExtension;

import java.util.*;

public class PlayerState implements BasePlayerState<BlockPos, Vec3> {
    private final Minecraft mc = Minecraft.getInstance();


    @Override
    public boolean isNull() {
        return mc.player == null;
    }

    @Override
    public boolean notNull() {
        return mc.player != null;
    }


    @Override
    public boolean isSurvivalOrAdventureMode() {
        return mc.gameMode == null || mc.gameMode.getPlayerMode().isSurvival();
    }


    @Override
    public double vectorX() {
        assert mc.player != null;
        return mc.player.getX();
    }

    @Override
    public double vectorY() {
        assert mc.player != null;
        return mc.player.getY();
    }

    @Override
    public double vectorZ() {
        assert mc.player != null;
        return mc.player.getZ();
    }

    @Override
    public Vec3 position() {
        assert mc.player != null;
        return mc.player.position();
    }

    @Override
    public Vec3 eyePosition() {
        assert mc.player != null;
        return mc.player.getEyePosition();
    }


    @Override
    public int blockX() {
        assert mc.player != null;
        return mc.player.getBlockX();
    }

    @Override
    public int blockY() {
        assert mc.player != null;
        return mc.player.getBlockY();
    }

    @Override
    public int blockZ() {
        assert mc.player != null;
        return mc.player.getBlockY();
    }

    @Override
    public BlockPos blockPos() {
        assert mc.player != null;
        return mc.player.blockPosition();
    }

    @Override
    public BlockPos eyeBlockPos() {
        assert mc.player != null;
        Vec3 eyePos = mc.player.getEyePosition();
        return BlockPos.containing(eyePos);
    }


    @Override
    public float health() {
        assert mc.player != null;
        return mc.player.getHealth();
    }

    @Override
    public float maxHealth() {
        assert mc.player != null;
        return mc.player.getMaxHealth();
    }

    @Override
    public List<String> getActiveEffectIds() {
        assert mc.player != null;
        return mc.player.getActiveEffectsMap().keySet().stream()
                .map(IHolderExtension::getKey)
                .filter(Objects::nonNull)
                .map(ResourceKey::location)
                .map(ResourceLocation::toString)
                .toList();
    }


    @Override
    public boolean isSleeping() {
        assert mc.player != null;
        return mc.player.isSleeping();
    }

    @Override
    public boolean isUnderwater() {
        assert mc.player != null;
        return mc.player.isUnderWater();
    }

    @Override
    public boolean isInLava() {
        assert mc.player != null;
        return mc.player.isInLava();
    }


    @Override
    public Optional<String> vehicleId() {
        assert mc.player != null;
        var vec = mc.player.getVehicle();
        return vec != null ? Optional.ofNullable(mc.player.getVehicle().getEncodeId()) : Optional.empty();
    }

    @Override
    public boolean inMinecart() {
        assert mc.player != null;
        return mc.player.getVehicle() instanceof Minecart;
    }

    @Override
    public boolean inBoat() {
        assert mc.player != null;
        return mc.player.getVehicle() instanceof Boat;
    }

    @Override
    public boolean onHorse() {
        assert mc.player != null;
        return mc.player.getVehicle() instanceof Horse;
    }

    @Override
    public boolean onDonkey() {
        assert mc.player != null;
        return mc.player.getVehicle() instanceof Donkey;
    }

    @Override
    public boolean onPig() {
        assert mc.player != null;
        return mc.player.getVehicle() instanceof Pig;
    }

    @Override
    public boolean elytraFlying() {
        assert mc.player != null;
        return mc.player.isFallFlying();
    }


    @Override
    public boolean fishingHookInWater() {
        assert mc.player != null;
        return mc.player.fishing != null && mc.player.fishing.isInWater();
    }

    @Override
    public boolean isInBossFight() {
        Map<UUID, LerpingBossEvent> bossMap =
                ObfuscationReflectionHelper.getPrivateValue(BossHealthOverlay.class, mc.gui.getBossOverlay(), "events");
        return bossMap != null && !bossMap.isEmpty();
    }

    @Override
    public List<String> getBosses() {
        Map<UUID, LerpingBossEvent> bossMap =
                ObfuscationReflectionHelper.getPrivateValue(BossHealthOverlay.class, mc.gui.getBossOverlay(), "events");

        return bossMap == null
                ? List.of()
                : bossMap.values()
                .stream()
                .map(bossEvent -> ((TranslatableContents)bossEvent.getName().getContents()).getKey())
                .toList();
    }

    @Override
    public double distanceTo(Vec3 position) {
        assert mc.player != null;
        return mc.player.position().distanceTo(position);
    }
}
