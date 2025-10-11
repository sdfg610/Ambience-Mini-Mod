package me.molybdenum.ambience_mini.setup;

import me.molybdenum.ambience_mini.engine.setup.BaseConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.function.Supplier;

public class Config extends BaseConfig
{
    private ForgeConfigSpec.Builder clientBuilder;


    public void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientBuilder.build());
    }


    @Override
    protected Supplier<Boolean> makeBoolOption(String comment, String name, boolean defaultValue) {
        return clientBuilder.comment(comment)
                .worldRestart()
                .define(name, defaultValue);
    }

    @Override
    protected Supplier<Integer> makeIntOption(String comment, String name, int defaultValue, int min, int max) {
        return clientBuilder.comment(comment)
                .worldRestart()
                .defineInRange(name, defaultValue, min, max);
    }

    @Override
    protected void preSetup() {
        clientBuilder = new ForgeConfigSpec.Builder();
        clientBuilder.comment("Ambience Mini Mod Configurations")
                .push("AmbienceMini");
    }

    @Override
    protected void postSetup() {
        clientBuilder.pop();
    }
}
