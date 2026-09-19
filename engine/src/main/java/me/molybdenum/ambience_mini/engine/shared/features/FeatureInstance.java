package me.molybdenum.ambience_mini.engine.shared.features;

import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public record FeatureInstance(Feature feature, boolean enabled) implements AmSerializable
{
    public FeatureInstance(AmReader reader) {
        this(Feature.decode(reader), reader.readBoolean());
    }

    public void configure(ServerSetup serverSetup) {
        if (feature != null)
            feature.configure(
                    enabled ? Availability.ENABLED : Availability.DISABLED,
                    serverSetup
            );
    }

    @Override
    public void writeTo(AmWriter writer) {
        writer.write(feature);
        writer.writeBoolean(enabled);
    }
}
