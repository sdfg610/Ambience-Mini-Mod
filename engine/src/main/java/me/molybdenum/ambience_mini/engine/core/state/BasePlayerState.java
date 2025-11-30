package me.molybdenum.ambience_mini.engine.core.state;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public interface BasePlayerState<TBlockPos, TVec3>
{
    // -----------------------------------------------------------------------------------------------------------------
    // Execution
    boolean isNull();
    boolean notNull();

    void prepare(@Nullable Logger logger);


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


    // -----------------------------------------------------------------------------------------------------------------
    // Utils
    double distanceTo(TVec3 position);
}
