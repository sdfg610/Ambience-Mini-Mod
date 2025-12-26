package me.molybdenum.ambience_mini.core.state;

import me.molybdenum.ambience_mini.engine.compatibility.EssentialCompat;
import me.molybdenum.ambience_mini.engine.core.state.BasePlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerState implements BasePlayerState<BlockPos, Vec3>
{
    private final Minecraft mc = Minecraft.getInstance();
    private LocalPlayer player = null;
    private MultiPlayerGameMode gameMode = null;


    @Override
    public boolean isNull() {
        return player == null || gameMode == null;
    }

    @Override
    public boolean notNull() {
        return player != null && gameMode != null;
    }


    @Override
    public void prepare(@Nullable ArrayList<String> messages) {
        LocalPlayer newPlayer = mc.player;
        if (EssentialCompat.isLoaded && EssentialCompat.tryCaptureFakes(newPlayer, (player) -> player.getGameProfile().getName(), LocalPlayer::level) && messages != null)
            messages.add("Captured fake player and world from essential mod!");

        if (player != newPlayer && EssentialCompat.isNotFakePlayer(newPlayer)) {
            if (messages != null)
                messages.add("Player instance changed from '" + getPlayerString(player) + "' to '" + getPlayerString(newPlayer) + "' since last update.");
            player = newPlayer;
        }
        gameMode = mc.gameMode;
    }

    private String getPlayerString(LocalPlayer pl) {
        return pl == null ? "null" : pl.getId() + "/" + System.identityHashCode(pl);
    }


    @Override
    public boolean isSurvivalOrAdventureMode() {
        assert gameMode != null;
        return gameMode.getPlayerMode().isSurvival();
    }

    @Override
    public String getGameMode() {
        assert gameMode != null;
        return gameMode.getPlayerMode().getName();
    }


    @Override
    public double vectorX() {
        assert player != null;
        return player.getX();
    }

    @Override
    public double vectorY() {
        assert player != null;
        return player.getY();
    }

    @Override
    public double vectorZ() {
        assert player != null;
        return player.getZ();
    }

    @Override
    public Vec3 position() {
        assert player != null;
        return player.position();
    }

    @Override
    public Vec3 eyePosition() {
        assert player != null;
        return player.getEyePosition();
    }


    @Override
    public int blockX() {
        assert player != null;
        return player.getBlockX();
    }

    @Override
    public int blockY() {
        assert player != null;
        return player.getBlockY();
    }

    @Override
    public int blockZ() {
        assert player != null;
        return player.getBlockY();
    }

    @Override
    public BlockPos blockPos() {
        assert player != null;
        return player.blockPosition();
    }

    @Override
    public BlockPos eyeBlockPos() {
        assert player != null;
        Vec3 eyePos = player.getEyePosition();
        return BlockPos.containing(eyePos);
    }


    @Override
    public float health() {
        assert player != null;
        return player.getHealth();
    }

    @Override
    public float maxHealth() {
        assert player != null;
        return player.getMaxHealth();
    }

    @Override
    public List<String> getActiveEffectIds() {
        assert player != null;
        return player.getActiveEffectsMap().keySet().stream()
                .map(ForgeRegistries.MOB_EFFECTS::getKey)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .toList();
    }


    @Override
    public boolean isSleeping() {
        assert player != null;
        return player.isSleeping();
    }

    @Override
    public boolean isUnderwater() {
        assert player != null;
        return player.isUnderWater();
    }

    @Override
    public boolean isInLava() {
        assert player != null;
        return player.isInLava();
    }

    @Override
    public boolean isDrowning() {
        assert player != null;
        return player.getAirSupply() <= 0;
    }


    @Override
    public Optional<String> vehicleId() {
        assert player != null;
        var vec = player.getVehicle();
        return vec != null ? Optional.ofNullable(player.getVehicle().getEncodeId()) : Optional.empty();
    }

    @Override
    public boolean inMinecart() {
        assert player != null;
        return player.getVehicle() instanceof Minecart;
    }

    @Override
    public boolean inBoat() {
        assert player != null;
        return player.getVehicle() instanceof Boat;
    }

    @Override
    public boolean onHorse() {
        assert player != null;
        return player.getVehicle() instanceof Horse;
    }

    @Override
    public boolean onDonkey() {
        assert player != null;
        return player.getVehicle() instanceof Donkey;
    }

    @Override
    public boolean onPig() {
        assert player != null;
        return player.getVehicle() instanceof Pig;
    }

    @Override
    public boolean elytraFlying() {
        assert player != null;
        return player.isFallFlying();
    }


    @Override
    public boolean fishingHookInWater() {
        assert player != null;
        return player.fishing != null && player.fishing.isInWater();
    }


    @Override
    public double distanceTo(Vec3 position) {
        assert player != null;
        return player.position().distanceTo(position);
    }
}
