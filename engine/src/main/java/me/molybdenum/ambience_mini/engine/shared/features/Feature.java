package me.molybdenum.ambience_mini.engine.shared.features;


import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.server.core.setup.BaseServerConfig;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;

public enum Feature implements AmSerializable {
    ////
    //// Absolutely do not change names after a release! This would break backwards compatibility.
    ////

    AREAS(
            "areas",
            config -> config.enableAreas.get(),
            (avail, setup) -> setup.areasFeature = avail
    ),
    FLAGS(
            "flags",
            config -> config.enableFlags.get(),
            (avail, setup) -> setup.flagsFeature = avail)
    //STRUCTURES("structures")

    ;

    public final String name;
    private final Function<BaseServerConfig, Boolean> isEnabled;
    private final BiConsumer<Availability, ServerSetup> setEnabled;

    Feature(
            String name,
            Function<BaseServerConfig, Boolean> isEnabled,
            BiConsumer<Availability, ServerSetup> setEnabled
    ) {
        this.name = name;
        this.isEnabled = isEnabled;
        this.setEnabled = setEnabled;
    }


    public FeatureInstance init(BaseServerConfig config) {
        return new FeatureInstance(this, isEnabled.apply(config));
    }

    public void configure(Availability avail, ServerSetup serverSetup) {
        setEnabled.accept(avail, serverSetup);
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(name);
    }

    public static Feature decode(AmReader reader) {
        var name = reader.readString();
        return Arrays.stream(Feature.values())
                .filter(flag -> flag.name.equals(name))
                .findFirst()
                .orElse(null);
    }
}
