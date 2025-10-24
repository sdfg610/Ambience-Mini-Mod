package me.molybdenum.ambience_mini.setup;

import me.molybdenum.ambience_mini.engine.setup.BaseConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Supplier;

public class Config extends BaseConfig
{
    private ModConfigSpec.Builder clientBuilder;


    public void register(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, clientBuilder.build());
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
        clientBuilder = new ModConfigSpec.Builder();
        clientBuilder.comment("Ambience Mini Mod Configurations")
                .push("AmbienceMini");
    }

    @Override
    protected void postSetup() {
        clientBuilder.pop();
    }
}
