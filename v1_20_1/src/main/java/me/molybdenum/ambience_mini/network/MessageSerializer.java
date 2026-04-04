package me.molybdenum.ambience_mini.network;

import io.netty.buffer.ByteBuf;
import me.molybdenum.ambience_mini.engine.shared.networking.serialization.AmSerializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;


public class MessageSerializer extends AmSerializer
{
    ByteBuf buffer;


    public MessageSerializer(ByteBuf buffer) {
        this.buffer = buffer;
    }


    @Override
    public boolean readBoolean() {
        return buffer.readBoolean();
    }

    @Override
    public void writeBoolean(boolean value) {
        buffer.writeBoolean(value);
    }


    @Override
    public int readInt() {
        return buffer.readInt();
    }

    @Override
    public void writeInt(int value) {
        buffer.writeInt(value);
    }


    @Override
    public double readDouble() {
        return buffer.readDouble();
    }

    @Override
    public void writeDouble(double value) {
        buffer.writeDouble(value);
    }


    @Override
    public String readString() {
        boolean hasValue = buffer.readBoolean();
        if (hasValue) {
            int length = buffer.readInt();
            ByteBuffer bytes = ByteBuffer.allocate(length);
            buffer.readBytes(bytes);
            bytes.rewind();
            return StandardCharsets.UTF_8.decode(bytes).toString();
        }
        return null;
    }

    @Override
    public void writeString(String value) {
        if (value == null)
            buffer.writeBoolean(false);
        else {
            buffer.writeBoolean(true);
            ByteBuffer bytes = StandardCharsets.UTF_8.encode(value);
            buffer.writeInt(bytes.limit());
            buffer.writeBytes(bytes);
        }
    }
}
