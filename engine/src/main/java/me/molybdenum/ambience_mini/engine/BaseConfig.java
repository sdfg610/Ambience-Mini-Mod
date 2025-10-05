package me.molybdenum.ambience_mini.engine;

import java.util.function.Supplier;

public abstract class BaseConfig {
    public final Supplier<Boolean> enabled;

    public final Supplier<Boolean> lostFocusEnabled;
    public final Supplier<Boolean> ignoreMasterVolume;

    public final Supplier<Integer> villageScanHorizontalRadius;
    public final Supplier<Integer> villageScanVerticalRadius;
    public final Supplier<Integer> villagerCountThreshold;

    public final Supplier<Integer> ranchScanHorizontalRadius;
    public final Supplier<Integer> ranchScanVerticalRadius;
    public final Supplier<Integer> animalCountThreshold;

    public final Supplier<Integer> fishingTimeout;
    public final Supplier<Integer> fishingMoveThreshold;

    public final Supplier<Integer> caveScoreRadius;
    public final Supplier<Integer> caveMeasureDistance;
    public final Supplier<Integer> yAxisGranularity;
    public final Supplier<Integer> xAxisGranularity;


    protected BaseConfig() {
        preSetup();

        enabled = makeBoolOption(
                "Whether the features of this mod should be enabled [Default:true]",
                "Enabled",
                true
        );

        // Volume control
        lostFocusEnabled = makeBoolOption(
                "Fade Out Sound Volume on Game Lost Focus [Default:true]",
                "Lost_Focus_FadeOut",
                true
        );
        ignoreMasterVolume = makeBoolOption(
                "If 'true', music volume is not affected by 'Master Volume' to make it easier to balance music volume with everything else [Default:true]",
                "Ignore_Master_Volume",
                true
        );

        // Village detection
        villageScanHorizontalRadius = makeIntOption(
                "The horizontal radius/distance to scan for villagers [Default:30]",
                "Village_Scan_Horizontal_Radius",
                30, 10, 100
        );
        villageScanVerticalRadius = makeIntOption(
                "The vertical radius/distance to scan for villagers [Default:15]",
                "Village_Scan_Vertical_Radius",
                15, 10, 100
        );
        villagerCountThreshold = makeIntOption(
                "The minimum number of villagers needed within the search box to count as a village [Default:3]",
                "Villager_Count_Threshold",
                3, 2, Integer.MAX_VALUE
        );

        // Ranch detection
        ranchScanHorizontalRadius = makeIntOption(
                "The horizontal radius/distance to scan for villagers [Default:30]",
                "Ranch_Scan_Horizontal_Radius",
                30, 10, 100
        );
        ranchScanVerticalRadius = makeIntOption(
                "The vertical radius/distance to scan for animals [Default:8]",
                "Ranch_Scan_Vertical_Radius",
                8, 5, 100
        );
        animalCountThreshold = makeIntOption(
                "The minimum number of animals needed within the search box to count as a ranch [Default:15]",
                "Animal_Count_Threshold",
                15, 5, Integer.MAX_VALUE
        );

        // Fishing
        fishingTimeout = makeIntOption(
                "If not having fished for 'Fishing_Timeout' milliseconds, the player will no longer be considered as fishing [Default:4000]",
                "Fishing_Timeout",
                4000, 500, 10000
        );
        fishingMoveThreshold = makeIntOption(
                "If not fishing and moving more than 'Fishing_Move_Threshold' blocks away from the latest fishing position, the player will no longer be considered as fishing [Default:1]",
                "Fishing_Move_Threshold",
                1, 1, 10
        );

        // Cave detection
        caveScoreRadius = makeIntOption(
                "The cave score is based on the average of all scores measured in the given radius from the player's eye-position. Higher values should yield smoother boundaries for entering/leaving caves. Only change if you experience odd music behavior when dealing with caves [Default:1]",
                "Cave_Score_Radius",
                1, 1, 10
        );
        caveMeasureDistance = makeIntOption(
                "The maximal distance in blocks from the player that the cave-detection algorithm will check to determine whether the player is in a cave [Default:123]",
                "Cave_Measure_Distance",
                123, 16, 500
        );
        yAxisGranularity = makeIntOption(
                "The number of vertical directions (in addition to straight up and down) to probe when determining whether the player is in a cave. Total directions: X_Axis_Granularity * Y_Axis_Granularity + 2 [Default:4]",
                "Y_Axis_Granularity",
                4, 1, 90
        );
        xAxisGranularity = makeIntOption(
                "The number of horizontal directions to probe when determining whether the player is in a cave. Total directions X_Axis_Granularity * Y_Axis_Granularity + 2 [Default:8]",
                "X_Axis_Granularity",
                8, 4, 180
        );

        postSetup();
    }

    @SuppressWarnings("SameParameterValue")
    protected abstract Supplier<Boolean> makeBoolOption(String comment, String name, boolean defaultValue);
    protected abstract Supplier<Integer> makeIntOption(String comment, String name, int defaultValue, int min, int max);

    protected abstract void preSetup();
    protected abstract void postSetup();
}
