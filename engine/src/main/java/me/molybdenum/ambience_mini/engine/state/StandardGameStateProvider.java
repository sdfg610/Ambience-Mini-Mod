package me.molybdenum.ambience_mini.engine.state;

import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.FloatT;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.IntT;
import me.molybdenum.ambience_mini.engine.loader.abstract_syntax.type.StringT;

public abstract class StandardGameStateProvider extends BaseGameStateProvider
{
    protected StandardGameStateProvider() {
        super();
        RegisterStandard();
    }


    private void RegisterStandard()
    {
        ////
        //// Events
        ////
        // Global events
        registerEvent("main_menu", this::inMainMenu);
        registerEvent("joining", this::isJoiningWorld);
        registerEvent("disconnected", this::isDisconnected);
        //registerEvent("paused", this::isPaused); // TODO: Paused should only work in single player
        registerEvent("in_game", this::inGame);
        registerEvent("credits", this::onCreditsScreen);

        // Time events
        registerEvent("day", this::isDay);
        registerEvent("dawn", this::isDawn);
        registerEvent("dusk", this::isDusk);
        registerEvent("night", this::isNight);

        // Weather
        registerEvent("downfall", this::isDownfall);
        registerEvent("rain", this::isRaining);
        registerEvent("snow", this::isSnowing);
        registerEvent("thunder", this::isThundering);

        // Special locations
        registerEvent("village", this::inVillage);
        registerEvent("ranch", this::inRanch);

        // Height-based
        registerEvent("under_deepslate", this::isUnderDeepslate);
        registerEvent("underground", this::isUnderground);
        registerEvent("under_water", this::isUnderWater);
        registerEvent("high_up", this::isHighUp);

        // Player state
        registerEvent("dead", this::isDead);
        registerEvent("sleeping", this::isSleeping);
        registerEvent("fishing", this::isFishing);

        // Mounts
        registerEvent("minecart", this::inMinecart);
        registerEvent("boat", this::inBoat);
        registerEvent("horse", this::onHorse);
        registerEvent("donkey", this::onDonkey);
        registerEvent("pig", this::onPig);
        //register("flying_elytra", () -> false); // TODO: Find out how to do this in 1.20.1

        // Combat
        registerEvent("in_combat", this::inCombat);
        registerEvent("boss_fight", this::inBossFight);
        registerEvent("in_raid", this::inRaid);


        ////
        //// Properties
        ////
        registerProperty("dimension", new StringT(), this::getDimensionId);
        registerProperty("biome", new StringT(), this::getBiomeId);
        registerProperty("time", new IntT(), this::getTime);

        registerProperty("vehicle", new StringT(), this::getVehicleId);

        registerProperty("elevation", new FloatT(), this::getPlayerElevation);

        registerProperty("boss", new StringT(), this::getBossId);

        //TODO: biometag, structure ?
    }


    // ----------------------------------------------------------------------------------------------------------------
    // Events
    public abstract boolean inMainMenu();
    public abstract boolean isJoiningWorld();
    public abstract boolean isDisconnected();
    public abstract boolean inGame();
    public abstract boolean onCreditsScreen();

    public abstract boolean isDay();
    public abstract boolean isDawn();
    public abstract boolean isDusk();
    public abstract boolean isNight();

    public abstract boolean isDownfall();
    public abstract boolean isRaining();
    public abstract boolean isSnowing();
    public abstract boolean isThundering();

    public abstract boolean inVillage();
    public abstract boolean inRanch();

    public abstract boolean isUnderDeepslate();
    public abstract boolean isUnderground();
    public abstract boolean isUnderWater();
    public abstract boolean isHighUp();

    public abstract boolean isDead();
    public abstract boolean isSleeping();
    public abstract boolean isFishing();

    public abstract boolean inMinecart();
    public abstract boolean inBoat();
    public abstract boolean onHorse();
    public abstract boolean onDonkey();
    public abstract boolean onPig();

    public abstract boolean inCombat();
    public abstract boolean inBossFight();
    public abstract boolean inRaid();


    // ----------------------------------------------------------------------------------------------------------------
    // Properties
    public abstract String getDimensionId();
    public abstract String getBiomeId();
    public abstract String getVehicleId();
    public abstract String getBossId();

    public abstract int getTime();

    public abstract float getPlayerElevation();
}
