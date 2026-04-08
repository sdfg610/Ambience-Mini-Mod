package me.molybdenum.ambience_mini.v1_21_1.client.core.state;

import me.molybdenum.ambience_mini.engine.client.core.state.BasePlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.extensions.IHolderExtension;

import java.lang.reflect.Method;
import java.util.*;


public class PlayerState extends BasePlayerState<BlockPos, Vec3, LocalPlayer> {
    private final Minecraft mc = Minecraft.getInstance();

    private static final Method getPlayerInfoMethod = ObfuscationReflectionHelper.findMethod(AbstractClientPlayer.class, "getPlayerInfo");
    private static final JukeboxHelper<SoundInstance, ChannelAccess.ChannelHandle> jukeboxHelper = new JukeboxHelper<>(
            ChannelAccess.ChannelHandle::isStopped,
            (instance) -> instance.getSource() == SoundSource.RECORDS,
            SoundInstance::getVolume,
            (instance) -> instance.getSound().getAttenuationDistance(),
            SoundInstance::getX, SoundInstance::getY, SoundInstance::getZ
    );


    @Override
    protected LocalPlayer getCurrentPlayer() {
        return mc.player;
    }

    @Override
    protected String getPlayerString(LocalPlayer pl) {
        return pl == null ? "null" : pl.getId() + "/" + System.identityHashCode(pl);
    }

    @Override
    protected String getName(LocalPlayer player) {
        return player.getGameProfile().getName();
    }

    @Override
    protected Object getLevel(LocalPlayer player) {
        return player.level();
    }


    @Override
    public String getUUID() {
        return cachedPlayer == null ? null : cachedPlayer.getStringUUID();
    }

    @Override
    public Boolean isSurvivalOrAdventureMode() {
        return cachedPlayer == null ? null : getGameMode().isSurvival();
    }

    @Override
    public String getGameModeName() {
        return cachedPlayer == null ? null : getGameMode().getName();
    }

    private GameType getGameMode() {
        try {
            return ((PlayerInfo)getPlayerInfoMethod.invoke(cachedPlayer)).getGameMode();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Boolean canHearJukeboxMusic() {
        if (cachedPlayer == null)
            return null;

        SoundEngine soundEngine = ObfuscationReflectionHelper.getPrivateValue(SoundManager.class, mc.getSoundManager(), "soundEngine");
        Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel = ObfuscationReflectionHelper.getPrivateValue(SoundEngine.class, soundEngine, "instanceToChannel");

        assert instanceToChannel != null;
        return jukeboxHelper.canHearJukebox(
                instanceToChannel,
                (x, y, z) -> Math.sqrt(cachedPlayer.getEyePosition().distanceToSqr(x, y, z))
        );
    }


    @Override
    public Double vectorY() {
        return cachedPlayer == null ? null : cachedPlayer.getY();
    }

    @Override
    public Vec3 position() {
        return cachedPlayer == null ? null : cachedPlayer.position();
    }

    @Override
    public Vec3 eyePosition() {
        return cachedPlayer == null ? null : cachedPlayer.getEyePosition();
    }


    @Override
    public BlockPos blockPos() {
        return cachedPlayer == null ? null : cachedPlayer.blockPosition();
    }

    @Override
    public BlockPos eyeBlockPos() {
        return cachedPlayer == null ? null : BlockPos.containing(cachedPlayer.getEyePosition());
    }


    @Override
    public Float health() {
        return cachedPlayer == null ? null : cachedPlayer.getHealth();
    }

    @Override
    public Float maxHealth() {
        return cachedPlayer == null ? null : cachedPlayer.getMaxHealth();
    }

    @Override
    public List<String> getActiveEffectIds() {
        return cachedPlayer == null ? null : cachedPlayer.getActiveEffectsMap().keySet().stream()
                .map(IHolderExtension::getKey)
                .filter(Objects::nonNull)
                .map(ResourceKey::location)
                .map(ResourceLocation::toString)
                .toList();
    }


    @Override
    public Boolean isSleeping() {
        return cachedPlayer == null ? null : cachedPlayer.isSleeping();
    }

    @Override
    public Boolean isUnderwater() {
        return cachedPlayer == null ? null : cachedPlayer.isUnderWater();
    }

    @Override
    public Boolean isInLava() {
        return cachedPlayer == null ? null : cachedPlayer.isInLava();
    }

    @Override
    public Boolean isDrowning() {
        return cachedPlayer == null ? null : cachedPlayer.getAirSupply() <= 0;
    }


    @Override
    public String vehicleId() {
        if (cachedPlayer == null)
            return null;

        var vec = cachedPlayer.getVehicle();
        return vec != null ? vec.getEncodeId() : "";
    }

    @Override
    public Boolean inMinecart() {
        return cachedPlayer == null ? null : cachedPlayer.getVehicle() instanceof Minecart;
    }

    @Override
    public Boolean inBoat() {
        return cachedPlayer == null ? null : cachedPlayer.getVehicle() instanceof Boat;
    }

    @Override
    public Boolean onHorse() {
        return cachedPlayer == null ? null : cachedPlayer.getVehicle() instanceof Horse;
    }

    @Override
    public Boolean onDonkey() {
        return cachedPlayer == null ? null : cachedPlayer.getVehicle() instanceof Donkey;
    }

    @Override
    public Boolean onPig() {
        return cachedPlayer == null ? null : cachedPlayer.getVehicle() instanceof Pig;
    }

    @Override
    public Boolean elytraFlying() {
        return cachedPlayer == null ? null : cachedPlayer.isFallFlying();
    }


    @Override
    public Boolean fishingHookInWater() {
        return cachedPlayer == null ? null : cachedPlayer.fishing != null && cachedPlayer.fishing.isInWater();
    }


    @Override
    public Double distanceTo(Vec3 position) {
        return cachedPlayer == null ? null : cachedPlayer.position().distanceTo(position);
    }
}
