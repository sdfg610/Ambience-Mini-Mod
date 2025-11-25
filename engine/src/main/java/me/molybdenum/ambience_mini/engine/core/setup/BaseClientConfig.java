package me.molybdenum.ambience_mini.engine.core.setup;

import java.util.function.Supplier;

public abstract class BaseClientConfig {
    // Misc
    public final Supplier<Boolean> notifyServerSupport;
    public final Supplier<Boolean> verboseMode;

    // Timing
    public final Supplier<Integer> updateInterval;
    public final Supplier<Integer> nextMusicDelay;
    public final Supplier<Boolean> meticulousPlaylistSelector;

    // Volume control
    public final Supplier<Boolean> lostFocusEnabled;
    public final Supplier<Boolean> ignoreMasterVolume;

    // Village detection
    public final Supplier<Integer> villageScanHorizontalRadius;
    public final Supplier<Integer> villageScanVerticalRadius;
    public final Supplier<Integer> villagerCountThreshold;

    // Ranch detection
    public final Supplier<Integer> ranchScanHorizontalRadius;
    public final Supplier<Integer> ranchScanVerticalRadius;
    public final Supplier<Integer> animalCountThreshold;

    // Fishing
    public final Supplier<Integer> fishingTimeout;
    public final Supplier<Integer> fishingMoveThreshold;

    // Cave detection
    public final Supplier<Integer> caveScoreRadius;
    public final Supplier<Integer> caveMeasureDistance;
    public final Supplier<Integer> yAxisGranularity;
    public final Supplier<Integer> xAxisGranularity;

    // Combat detection
    public final Supplier<Integer> combatGracePeriod;
    public final Supplier<Integer> combatantTimeout;
    public final Supplier<Integer> leavingCombatDistance;


    protected BaseClientConfig() {
        preSetup();

        notifyServerSupport = makeBoolOption(
                "If enabled, upon joining a multiplayer server the client will notify whether the server has support for Ambience Mini. If no support, server-dependent features will be disabled. [Default: true]",
                "Notify_Server_Support",
                true
        );

        verboseMode = makeBoolOption(
                "If enabled, the mod will print debugging information to the terminal whenever the current playlist changes. Used for troubleshooting. [Default: false]",
                "Verbose_Mode",
                false
        );

        // Timing
        updateInterval = makeIntOption(
                "The interval in milliseconds between checking the environment and possibly picking new music. [Default: 100]",
                "Update_Interval",
                100, 50, 10000
        );
        nextMusicDelay = makeIntOption(
                "The delay in milliseconds from when some music reaches the end until the next music begins. [Default: 4000]",
                "Next_Music_Delay",
                4000, 1000, 60000
        );

        meticulousPlaylistSelector = makeBoolOption(
                "If enabled, the mod will double-check playlist selections before switching. This can be helpful to counteract unpredictable/sporadic effects from mod-interactions. Note that this option doubles the time it takes to change to a new playlist, but this can be corrected by decreasing the 'Update_Interval' if you feel the need. [Default: false]",
                "Meticulous_Playlist_Selector",
                false
        );

        // Volume control
        lostFocusEnabled = makeBoolOption(
                "Fade Out Sound Volume on Game Lost Focus. [Default: true]",
                "Lost_Focus_FadeOut",
                true
        );
        ignoreMasterVolume = makeBoolOption(
                "If 'true', music volume is not affected by 'Master Volume' to make it easier to balance music volume with everything else. [Default: true]",
                "Ignore_Master_Volume",
                true
        );

        // Village detection
        villageScanHorizontalRadius = makeIntOption(
                "The horizontal radius/distance to scan for villagers. [Default: 30]",
                "Village_Scan_Horizontal_Radius",
                30, 10, 100
        );
        villageScanVerticalRadius = makeIntOption(
                "The vertical radius/distance to scan for villagers. [Default: 15]",
                "Village_Scan_Vertical_Radius",
                15, 10, 100
        );
        villagerCountThreshold = makeIntOption(
                "The minimum number of villagers needed within the search box to count as a village. [Default: 3]",
                "Villager_Count_Threshold",
                3, 2, Integer.MAX_VALUE
        );

        // Ranch detection
        ranchScanHorizontalRadius = makeIntOption(
                "The horizontal radius/distance to scan for villagers. [Default: 30]",
                "Ranch_Scan_Horizontal_Radius",
                30, 10, 100
        );
        ranchScanVerticalRadius = makeIntOption(
                "The vertical radius/distance to scan for animals. [Default: 8]",
                "Ranch_Scan_Vertical_Radius",
                8, 5, 100
        );
        animalCountThreshold = makeIntOption(
                "The minimum number of animals needed within the search box to count as a ranch. [Default: 15]",
                "Animal_Count_Threshold",
                15, 5, Integer.MAX_VALUE
        );

        // Fishing
        fishingTimeout = makeIntOption(
                "If not having fished for 'Fishing_Timeout' milliseconds, the player will no longer be considered as fishing. [Default: 4000]",
                "Fishing_Timeout",
                4000, 500, 10000
        );
        fishingMoveThreshold = makeIntOption(
                "If not fishing and moving more than 'Fishing_Move_Threshold' blocks away from the latest fishing position, the player will no longer be considered as fishing.[Default: 1]",
                "Fishing_Move_Threshold",
                1, 1, 10
        );

        // Cave detection
        caveScoreRadius = makeIntOption(
                "The cave score is based on the average of all scores measured in the given radius from the player's eye-position. Higher values should yield smoother boundaries for entering/leaving caves. Only change if you experience odd music behavior when dealing with caves. [Default: 1]",
                "Cave_Score_Radius",
                1, 1, 10
        );
        caveMeasureDistance = makeIntOption(
                "The maximal distance in blocks from the player that the cave-detection algorithm will check to determine whether the player is in a cave. [Default: 123]",
                "Cave_Measure_Distance",
                123, 16, 500
        );
        yAxisGranularity = makeIntOption(
                "The number of vertical directions (in addition to straight up and down) to probe when determining whether the player is in a cave. Total directions: X_Axis_Granularity * Y_Axis_Granularity + 2. [Default: 5]",
                "Y_Axis_Granularity",
                5, 1, 90
        );
        xAxisGranularity = makeIntOption(
                "The number of horizontal directions to probe when determining whether the player is in a cave. Total directions X_Axis_Granularity * Y_Axis_Granularity + 2. [Default: 12]",
                "X_Axis_Granularity",
                12, 4, 180
        );

        // Combat detection
        combatGracePeriod = makeIntOption(
                "After leaving combat, the 'in_combat' event will stay active for the additional time (in milliseconds) given here, allowing new enemies to join the fight without music switching between combat and non-combat music. [Default: 2500]",
                "Combat_Grace_Period",
                3500, 1000, 10000
        );

        combatantTimeout = makeIntOption(
                "Only used when no server-support is detected. If no interaction has been made between a player and a mob for the time given here (in milliseconds), these will no longer be considered 'in combat'. [Default: 5000]",
                "Combatant_Timeout",
                5000, 2500, 15000
        );
        leavingCombatDistance = makeIntOption(
                "Only used when no server-support is detected. When the player moves the number of blocks away from a mob given here, these will no longer be considered 'in combat'. [Default: 20]",
                "Leaving_Combat_Distance",
                20, 10, 50
        );

        postSetup();
    }

    protected abstract Supplier<Boolean> makeBoolOption(String comment, String name, boolean defaultValue);
    protected abstract Supplier<Integer> makeIntOption(String comment, String name, int defaultValue, int min, int max);

    protected abstract void preSetup();
    protected abstract void postSetup();
}
