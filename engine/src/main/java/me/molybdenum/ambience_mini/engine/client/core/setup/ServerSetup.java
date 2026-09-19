package me.molybdenum.ambience_mini.engine.client.core.setup;

import me.molybdenum.ambience_mini.engine.shared.features.Availability;
import me.molybdenum.ambience_mini.engine.shared.features.FeatureInstance;
import me.molybdenum.ambience_mini.engine.shared.utils.versions.AmVersion;

import java.util.List;

public class ServerSetup
{
    public AmVersion serverVersion;
    public boolean isOnLocalServer;

    public Availability areasFeature;
    public Availability flagsFeature;


    public ServerSetup() {
        reset();
    }


    public void reset() {
        serverVersion = AmVersion.ZERO;
        isOnLocalServer = false;

        areasFeature = Availability.NOT_SUPPORTED;
        flagsFeature = Availability.NOT_SUPPORTED;
    }


    public void setServerVersion(AmVersion version) {
        serverVersion = version;

        var ltV2_8_0 = serverVersion.isLessThan(AmVersion.V_2_8_0);

        if (serverVersion.isGreaterThanOrEqual(AmVersion.V_2_5_0) && ltV2_8_0)
            areasFeature = Availability.ENABLED;

        if (serverVersion.isGreaterThanOrEqual(AmVersion.V_2_6_0) && ltV2_8_0)
            flagsFeature = Availability.ENABLED;
    }

    public void setFeatures(List<FeatureInstance> features) {
        for (var inst : features)
            inst.configure(this);
    }
}
