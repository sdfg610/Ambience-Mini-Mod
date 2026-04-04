package me.molybdenum.ambience_mini.engine.client.core.state;

import me.molybdenum.ambience_mini.engine.shared.utils.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface BasePlayerState<TBlockPos, TVec3>
{
    // -----------------------------------------------------------------------------------------------------------------
    // Execution
    boolean isNull();
    boolean notNull();

    void prepare(@Nullable ArrayList<String> messages);


    // -----------------------------------------------------------------------------------------------------------------
    // Player state
    String getUUID();
    Boolean isSurvivalOrAdventureMode();
    String getGameModeName();

    Boolean canHearJukeboxMusic();

    Double vectorX();
    Double vectorY();
    Double vectorZ();
    TVec3 position();
    TVec3 eyePosition();

    Integer blockX();
    Integer blockY();
    Integer blockZ();
    TBlockPos blockPos();
    TBlockPos eyeBlockPos();

    Float health();
    Float maxHealth();
    List<String> getActiveEffectIds();

    Boolean isSleeping();
    Boolean isUnderwater();
    Boolean isInLava();
    Boolean isDrowning();

    String vehicleId();
    Boolean inMinecart();
    Boolean inBoat();
    Boolean onHorse();
    Boolean onDonkey();
    Boolean onPig();
    Boolean elytraFlying();

    Boolean fishingHookInWater();


    // -----------------------------------------------------------------------------------------------------------------
    // Utils
    Double distanceTo(TVec3 position);


    class JukeboxHelper<TSoundInstance, TChannelHandle> {
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
