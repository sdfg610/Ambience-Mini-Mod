package me.molybdenum.ambience_ide.parsers;

import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.FloatVal;

import java.util.Optional;
import java.util.regex.Pattern;

public class FloatParser {
    public static final String floatPattern = "[0-9]+(\\.[0-9]*)?";

    public static final Pattern floatValPattern = Pattern.compile(
            floatPattern + "|undefined"
    );


    public static Optional<String> validate(String value) {
        var trim = value.trim();
        return floatValPattern.matcher(trim).matches() ? Optional.empty() : Optional.of("'" + trim + "' is not a valid float value.");
    }

    public static FloatVal parse(String value) {
        try {
            return new FloatVal(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            return FloatVal.UNDEFINED;
        }
    }
}
