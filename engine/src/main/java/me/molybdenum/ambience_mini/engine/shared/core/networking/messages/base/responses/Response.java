package me.molybdenum.ambience_mini.engine.shared.core.networking.messages.base.responses;

import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.helper.HelperReader;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;

import java.util.ArrayList;
import java.util.function.Function;

public class Response implements AmSerializable
{
    public final byte[] data;
    public final Text error;


    public Response(byte[] data) {
        this.data = data;
        this.error = null;
    }

    public Response(Text error) {
        this.data = null;
        this.error = error;
    }

    public Response(AmReader reader) {
        this.data = reader.readBoolean() ? reader.readByteArray() : null;
        this.error = reader.readBoolean() ? new Text(reader) : null;
    }


    public boolean isSuccess() {
        return error == null;  // Success can have null data, so check error instead
    }

    public <T> T decode(Function<AmReader, T> ctor) {
        return ctor.apply(new HelperReader(data));
    }

    public <T extends AmSerializable> ArrayList<T> decodeList(Function<AmReader, T> ctor) {
        return new HelperReader(data).readList(ctor);
    }


    @Override
    public void writeTo(AmWriter writer) {
        if (data == null)
            writer.writeBoolean(false);
        else {
            writer.writeBoolean(true);
            writer.writeByteArray(data);
        }

        if (error == null)
            writer.writeBoolean(false);
        else {
            writer.writeBoolean(true);
            error.writeTo(writer);
        }
    }
}
