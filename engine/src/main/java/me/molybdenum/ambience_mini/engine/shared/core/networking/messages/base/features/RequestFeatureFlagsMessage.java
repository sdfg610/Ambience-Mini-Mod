package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.features;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class RequestFeatureFlagsMessage extends AmMessage {
    public RequestFeatureFlagsMessage(AmReader ignored) { }

    public RequestFeatureFlagsMessage() { }


    @Override
    public void writeTo(AmWriter ignored) { }
}
