package me.molybdenum.ambience_mini.core.state;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.compatibility.EssentialCompat;
import me.molybdenum.ambience_mini.engine.core.state.BasePlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

public class PlayerState implements BasePlayerState<BlockPos, Vec3>
{
    private final Minecraft mc = Minecraft.getInstance();
    private LocalPlayer player = null;

    private static final Method getPlayerInfoMethod = ObfuscationReflectionHelper.findMethod(AbstractClientPlayer.class, "m_108558_");
    private static final String OBF_SOUND_ENGINE = "f_120349_";
    private static final String OBF_INSTANCE_TO_CHANNEL = "f_120226_";

    private static final JukeboxHelper<SoundInstance, ChannelAccess.ChannelHandle> jukeboxHelper = new JukeboxHelper<>(
            ChannelAccess.ChannelHandle::isStopped,
            (instance) -> instance.getSource() == SoundSource.RECORDS,
            SoundInstance::getVolume,
            (instance) -> instance.getSound().getAttenuationDistance(),
            SoundInstance::getX, SoundInstance::getY, SoundInstance::getZ
    );


    @Override
    public boolean isNull() {
        return player == null;
    }

    @Override
    public boolean notNull() {
        return player != null;
    }


    @Override
    public void prepare(@Nullable ArrayList<String> messages) {
        LocalPlayer newPlayer = mc.player;
        if (EssentialCompat.isLoaded && EssentialCompat.tryCaptureFakes(newPlayer, (player) -> player.getGameProfile().getName(), LocalPlayer::getLevel) && messages != null)
            messages.add("Captured fake player and world from essential mod!");

        if (player != newPlayer && EssentialCompat.isNotFakePlayer(newPlayer)) {
            if (messages != null)
                messages.add("Player instance changed from '" + getPlayerString(player) + "' to '" + getPlayerString(newPlayer) + "' since last update.");
            player = newPlayer;
        }
    }

    private String getPlayerString(LocalPlayer pl) {
        return pl == null ? "null" : pl.getId() + "/" + System.identityHashCode(pl);
    }


    @Override
    public boolean isSurvivalOrAdventureMode() {
        assert player != null;
        return getGameMode().isSurvival();
    }

    @Override
    public String getGameModeName() {
        assert player != null;
        return getGameMode().getName();
    }

    private GameType getGameMode() {
        try {
            return ((PlayerInfo)getPlayerInfoMethod.invoke(player)).getGameMode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean canHearJukeboxMusic() {
        assert player != null;
        SoundEngine soundEngine = ObfuscationReflectionHelper.getPrivateValue(SoundManager.class, mc.getSoundManager(), OBF_SOUND_ENGINE);
        Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = ObfuscationReflectionHelper.getPrivateValue(SoundEngine.class, soundEngine, OBF_INSTANCE_TO_CHANNEL);

        assert instanceToChannel != null;
        return jukeboxHelper.canHearJukebox(
                instanceToChannel,
                (x, y, z) -> Math.sqrt(player.getEyePosition().distanceToSqr(x, y, z))
        );
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
        return player.eyeBlockPosition();
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
                .map(ForgeRegistryEntry::getRegistryName)
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
    public String vehicleId() {
        assert player != null;
        var vec = player.getVehicle();
        return vec != null ? vec.getEncodeId() : "";
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
        return player.fishing != null && (player.fishing.isOpenWaterFishing() || player.fishing.isInWater());
    }


    @Override
    public double distanceTo(Vec3 position) {
        assert player != null;
        return player.position().distanceTo(position);
    }
}
