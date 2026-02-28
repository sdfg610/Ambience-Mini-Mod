package me.molybdenum.ambience_mini.engine.shared.networking.serialization;

public interface AmSerializable {
    void writeTo(AmWriter writer);
    void readFrom(AmReader reader);
}
