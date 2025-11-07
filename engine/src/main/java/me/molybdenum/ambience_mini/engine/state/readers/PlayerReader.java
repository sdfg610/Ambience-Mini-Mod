package me.molybdenum.ambience_mini.engine.state.readers;

import java.util.Optional;

public interface PlayerReader<TBlockPos, TVec3> {
    boolean isNull();
    boolean notNull();

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

    Optional<String> getBossIdIfInFight();

    double distanceTo(TVec3 position);
}
