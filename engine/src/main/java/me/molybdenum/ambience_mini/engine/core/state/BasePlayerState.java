package me.molybdenum.ambience_mini.engine.core.state;

import me.molybdenum.ambience_mini.engine.utils.TriFunction;
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
    boolean isSurvivalOrAdventureMode();
    String getGameModeName();

    boolean canHearJukeboxMusic();

    double vectorX();
    double vectorY();
    double vectorZ();
    TVec3 position();
    TVec3 eyePosition();

    int blockX();
    int blockY();
    int blockZ();
    TBlockPos blockPos();
    TBlockPos eyeBlockPos();

    float health();
    float maxHealth();
    List<String> getActiveEffectIds();

    boolean isSleeping();
    boolean isUnderwater();
    boolean isInLava();
    boolean isDrowning();

    String vehicleId();
    boolean inMinecart();
    boolean inBoat();
    boolean onHorse();
    boolean onDonkey();
    boolean onPig();
    boolean elytraFlying();

    boolean fishingHookInWater();


    // -----------------------------------------------------------------------------------------------------------------
    // Utils
    double distanceTo(TVec3 position);



    class JukeboxHelper<TSoundInstance, TChannelHandle> {
        private final Function<TChannelHandle, Boolean> isStopped;

        private final Function<TSoundInstance, Boolean> isRecordSource;
        private final Function<TSoundInstance, Float> getVolume;
        private final Function<TSoundInstance, Integer> getAttenuationDistance;
        private final Function<TSoundInstance, Double> getX;
        private final Function<TSoundInstance, Double> getY;
        private final Function<TSoundInstance, Double> getZ;

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
            return instanceToChannel.entrySet().stream().anyMatch(entry -> {
                TSoundInstance instance = entry.getKey();
                if (!isRecordSource.apply(instance) || isStopped.apply(entry.getValue()))
                    return false;

                double distanceToMusic = getDistanceToPlayerFrom.apply(getX.apply(instance), getY.apply(instance), getZ.apply(instance));
                double musicRange = Math.max(getVolume.apply(instance), 1.0F) * getAttenuationDistance.apply(instance) * (0.75 + (VolumeState.getTrueRecordVolume() * 0.25)); // Music range scales really weirdly with record volume...
                return distanceToMusic < musicRange;
            });
        }
    }
}
