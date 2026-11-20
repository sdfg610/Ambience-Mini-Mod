package me.molybdenum.ambience_mini.engine.shared.core.networking.serialization;

import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.helper.HelperWriter;

public interface AmSerializable {
    void writeTo(AmWriter writer);

    default byte[] toBytes() {
        var writer = new HelperWriter();
        writeTo(writer);
        return writer.getBytes();
    }
}
