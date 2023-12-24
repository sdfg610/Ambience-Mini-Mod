package gsto.ambience_mini.setup;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config
{
    public static ForgeConfigSpec.BooleanValue enabled;
    public static ForgeConfigSpec.BooleanValue lostFocusEnabled;
    public static ForgeConfigSpec.IntValue attackedDistance;
    public static ForgeConfigSpec.BooleanValue ignoreMasterVolume;


    public static void register()
    {
        registerClientConfigs();
    }

    public static void registerClientConfigs()
    {
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();

        clientBuilder.comment("Ambience Mini Mod Configurations")
            .push("AmbienceMini");

        enabled = clientBuilder.comment("Whether the features of this mod should be enabled")
                .worldRestart()
                .define("Enabled", true);

        lostFocusEnabled = clientBuilder.comment("Fade Out Sound Volume on Game Lost Focus [Default:true]")
                .worldRestart()
                .define("Lost_Focus_FadeOut", true);

        attackedDistance = clientBuilder.comment("Defines the distance in blocks between the player and hostile mobs to determine if still in combat or not [Default:16,Range:10 ~ 128]")
                .worldRestart()
                .defineInRange("In_Battle_Distance",16,10,128);

        ignoreMasterVolume = clientBuilder.comment("If 'true', music volume is not affected by 'Master Volume' to make it easier to balance music volume with everything else [Default:true]")
                .worldRestart()
                .define("Ignore_Master_Volume",true);


        clientBuilder.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientBuilder.build());
    }
}
