package me.molybdenum.ambience_mini.engine.shared.utils;

import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmReader;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmSerializable;
import me.molybdenum.ambience_mini.engine.shared.core.networking.serialization.AmWriter;

import java.util.Arrays;
import java.util.List;

public class Text implements AmSerializable {
    public boolean isLiteral;
    public String base;
    public String[] args;


    private Text(boolean isLiteral, String base, String[] args) {
        this.isLiteral = isLiteral;
        this.base = base;
        this.args = args;
    }

    public Text(AmReader reader) {
        this(reader.readBoolean(), reader.readString(), reader.readStringArray());
    }


    public static Text ofLiteral(String literalText) {
        return new Text(true, literalText, new String[0]);
    }

    public static Text ofTranslatable(AmLang key, String... args) {
        if (key.argCount != args.length)
            throw new RuntimeException("Language key '" + key.name() + "' expects '" + key.argCount + "' arguments, but got '" + args.length + "'");
        return new Text(false, key.key, args);
    }


    @Override
    public void writeTo(AmWriter writer) {
        writer.writeBoolean(isLiteral);
        writer.writeString(base);
        writer.writeStringArray(args);
    }
}
