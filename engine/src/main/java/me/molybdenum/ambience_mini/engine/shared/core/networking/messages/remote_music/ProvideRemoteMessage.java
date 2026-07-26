package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.remote_music;

import me.molybdenum.ambience_mini.engine.shared.core.networking.messages.AmMessage;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

public class ProvideRemoteMessage extends AmMessage {
    // TODO: Remote Chunk Data

    public ProvideRemoteMessage() {
    }

    public ProvideRemoteMessage(AmReader reader) {
        this();
    }

    @Override
    public void writeTo(AmWriter writer) {

    }
}
