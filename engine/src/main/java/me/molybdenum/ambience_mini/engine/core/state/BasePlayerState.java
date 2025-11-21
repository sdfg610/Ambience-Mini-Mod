package me.molybdenum.ambience_mini.engine.core.state;

import java.util.List;
import java.util.Optional;

public interface BasePlayerState<TBlockPos, TVec3> {
    boolean isNull();
    boolean notNull();


    // -----------------------------------------------------------------------------------------------------------------
    // Player state
    boolean isSurvivalOrAdventureMode();

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

    Optional<String> vehicleId();
    boolean inMinecart();
    boolean inBoat();
    boolean onHorse();
    boolean onDonkey();
    boolean onPig();
    boolean elytraFlying();

    boolean fishingHookInWater();

    boolean isInBossFight();
    List<String> getBosses();


    // -----------------------------------------------------------------------------------------------------------------
    // Utils
    double distanceTo(TVec3 position);
}
