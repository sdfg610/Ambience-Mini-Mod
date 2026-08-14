package me.molybdenum.ambience_mini.engine.shared.music.music_dto;

import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

import java.util.Optional;

public abstract class ValueDTO implements AmSerializable
{
    protected abstract String getTypeID();
    protected abstract void innerWriteTo(AmWriter writer);

    public abstract int getSerializedLength();


    public Boolean tryGetBoolean() {
        return this instanceof BoolDTO b ? b.value : null;
    }

    public Float tryGetFloat() {
        return this instanceof FloatDTO f ? f.value : null;
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeString(getTypeID());
        innerWriteTo(writer);
    }

    public static ValueDTO readFrom(AmReader reader) {
        return switch (reader.readString()) {
            case BOOL_ID -> BoolDTO.ctor(reader);
            case FLOAT_ID -> FloatDTO.ctor(reader);
            default -> null;
        };
    }


    private static final String BOOL_ID = "bool";
    public static class BoolDTO extends ValueDTO {
        public final boolean value;


        public BoolDTO(boolean value) {
            this.value = value;
        }


        @Override
        protected String getTypeID() {
            return BOOL_ID;
        }

        @Override
        protected void innerWriteTo(AmWriter writer) {
            writer.writeBoolean(value);
        }

        @Override
        public int getSerializedLength() {
            return 1;
        }


        @Override
        public String toString() {
            return Boolean.toString(value);
        }


        private static BoolDTO ctor(AmReader reader) {
            return new BoolDTO(reader.readBoolean());
        }
    }

    private static final String FLOAT_ID = "float";
    public static class FloatDTO extends ValueDTO {
        public final float value;


        public FloatDTO(float value) {
            this.value = value;
        }


        @Override
        protected String getTypeID() {
            return FLOAT_ID;
        }

        @Override
        protected void innerWriteTo(AmWriter writer) {
            writer.writeFloat(value);
        }

        @Override
        public int getSerializedLength() {
            return Float.BYTES;
        }


        @Override
        public String toString() {
            return Float.toString(value);
        }


        private static FloatDTO ctor(AmReader reader) {
            return new FloatDTO(reader.readFloat());
        }
    }
}
