package me.molybdenum.ambience_mini.v1_20_1.server.core.setup;

import me.molybdenum.ambience_mini.engine.server.core.setup.BaseServerConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Supplier;

public class ServerConfig extends BaseServerConfig
{
    private ForgeConfigSpec.Builder configBuilder;


    public ServerConfig(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.SERVER, configBuilder.build());
    }


    @Override
    protected Supplier<Boolean> makeBoolOption(String comment, String name, boolean defaultValue) {
        return configBuilder.comment(comment)
                .worldRestart()
                .define(name, defaultValue);
    }

    @Override
    protected Supplier<Integer> makeIntOption(String comment, String name, int defaultValue, int min, int max) {
        return configBuilder.comment(comment)
                .worldRestart()
                .defineInRange(name, defaultValue, min, max);
    }

    @Override
    protected void preSetup() {
        configBuilder = new ForgeConfigSpec.Builder();
        configBuilder.comment("Ambience Mini Mod Configurations")
                .push("AmbienceMini");
    }

    @Override
    protected void postSetup() {
        configBuilder.pop();
    }
}
