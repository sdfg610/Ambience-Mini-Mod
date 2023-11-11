package gsto.ambience_mini.setup;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class Config
{
    public static ForgeConfigSpec.BooleanValue lostFocusEnabled;
    public static ForgeConfigSpec.IntValue fadeDuration;
    public static ForgeConfigSpec.IntValue attackedDistance;


    public static void register()
    {
        registerClientConfigs();
    }

    public static void registerClientConfigs()
    {
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();

        clientBuilder.comment("Ambience Mini Mod Configurations")
            .push("AmbienceMini");

        lostFocusEnabled = clientBuilder.comment("Fade Out Sound Volume on Game Lost Focus [Default:true]")
                .worldRestart()
                .define("Lost_Focus_FadeOut", true);

        fadeDuration = clientBuilder.comment("Defines the sound volume fade in/out duration [Default:25,Range:1~500]")
                .worldRestart()
                .defineInRange("Fade_Duration",25,1,500);

        attackedDistance = clientBuilder.comment("Defines the distance in blocks between the player and hostile mobs to determine if still in combat or not [Default:16,Range:10 ~ 128]")
                .worldRestart()
                .defineInRange("In_Battle_Distance",16,10,128);


        clientBuilder.pop();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientBuilder.build());
    }
}
