package me.molybdenum.ambience_mini.engine.client.core.state;

import me.molybdenum.ambience_mini.engine.shared.compatibility.EssentialCompat;
import me.molybdenum.ambience_mini.engine.shared.utils.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class BasePlayerState<TBlockPos, TVec3, TLocalPlayer>
{
    protected TLocalPlayer cachedPlayer = null;

    // -----------------------------------------------------------------------------------------------------------------
    // Execution
    public boolean isNull() {
        return cachedPlayer == null;
    }

    public boolean notNull() {
        return cachedPlayer != null;
    }


    public void prepare(@Nullable ArrayList<String> messages) {
        TLocalPlayer newPlayer = getCurrentPlayer();
        if (EssentialCompat.isLoaded && EssentialCompat.tryCaptureFakes(newPlayer, this::getName, this::getLevel) && messages != null)
            messages.add("Captured fake player and world from essential mod!");

        if (cachedPlayer != newPlayer && EssentialCompat.isNotFakePlayer(newPlayer)) {
            if (messages != null)
                messages.add("Player instance changed from '" + getPlayerString(cachedPlayer) + "' to '" + getPlayerString(newPlayer) + "' since last update.");
            cachedPlayer = newPlayer;
        }
    }

    protected abstract TLocalPlayer getCurrentPlayer();
    protected abstract String getPlayerString(TLocalPlayer player);
    protected abstract String getName(TLocalPlayer player);
    protected abstract Object getLevel(TLocalPlayer player);


    // -----------------------------------------------------------------------------------------------------------------
    // Player state
    public abstract String getUUID();
    public abstract Boolean isSurvivalOrAdventureMode();
    public abstract String getGameModeName();

    public abstract Boolean canHearJukeboxMusic();

    public abstract Double vectorX();
    public abstract Double vectorY();
    public abstract Double vectorZ();
    public abstract TVec3 position();
    public abstract TVec3 eyePosition();

    public abstract Integer blockX();
    public abstract Integer blockY();
    public abstract Integer blockZ();
    public abstract TBlockPos blockPos();
    public abstract TBlockPos eyeBlockPos();

    public abstract Float health();
    public abstract Float maxHealth();
    public abstract List<String> getActiveEffectIds();

    public abstract Boolean isSleeping();
    public abstract Boolean isUnderwater();
    public abstract Boolean isInLava();
    public abstract Boolean isDrowning();

    public abstract String vehicleId();
    public abstract Boolean inMinecart();
    public abstract Boolean inBoat();
    public abstract Boolean onHorse();
    public abstract Boolean onDonkey();
    public abstract Boolean onPig();
    public abstract Boolean elytraFlying();

    public abstract Boolean fishingHookInWater();


    // -----------------------------------------------------------------------------------------------------------------
    // Utils
    public abstract Double distanceTo(TVec3 position);


    public static class JukeboxHelper<TSoundInstance, TChannelHandle> {
        private final Function<TChannelHandle, Boolean> isStopped;

        private final Function<TSoundInstance, Boolean> isRecordSource;
        private final Function<TSoundInstance, Float> getVolume;
        private final Function<TSoundInstance, Integer> getAttenuationDistance;
        private final Function<TSoundInstance, Double> getX;
        private final Function<TSoundInstance, Double> getY;
        private final Function<TSoundInstance, Double> getZ;

        private boolean latestResult = false;


        public JukeboxHelper(
                Function<TChannelHandle, Boolean> isStopped,
                Function<TSoundInstance, Boolean> isRecordSource,
                Function<TSoundInstance, Float> getVolume,
                Function<TSoundInstance, Integer> getAttenuationDistance,
                Function<TSoundInstance, Double> getX,
                Function<TSoundInstance, Double> getY,
                Function<TSoundInstance, Double> getZ
        ) {
            this.isStopped = isStopped;

            this.isRecordSource = isRecordSource;
            this.getVolume = getVolume;
            this.getAttenuationDistance = getAttenuationDistance;
            this.getX = getX;
            this.getY = getY;
            this.getZ = getZ;
        }

        public boolean canHearJukebox(Map<TSoundInstance, TChannelHandle> instanceToChannel, TriFunction<Double, Double, Double, Double> getDistanceToPlayerFrom) {
            try {
                ArrayList<Map.Entry<TSoundInstance, TChannelHandle>> sounds = new ArrayList<>(instanceToChannel.entrySet()); // Should counteract most concurrent modification exceptions.
                latestResult = sounds.stream().anyMatch(entry -> {
                    TSoundInstance instance = entry.getKey();
                    if (!isRecordSource.apply(instance) || isStopped.apply(entry.getValue()))
                        return false;

                    double distanceToMusic = getDistanceToPlayerFrom.apply(getX.apply(instance), getY.apply(instance), getZ.apply(instance));
                    double musicRange = Math.max(getVolume.apply(instance), 1.0F) * getAttenuationDistance.apply(instance) * (0.75 + (VolumeState.getTrueRecordVolume() * 0.25)); // Music range scales really weirdly with record volume...
                    return distanceToMusic < musicRange;
                });
            }
            catch (Exception ignored) { } // We might hit a concurrent modification exception. Just ignore it cus' its not the end of thw world and I can't find a simple way to fix it.

            return latestResult;
        }
    }
}
