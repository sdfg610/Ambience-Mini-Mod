package me.molybdenum.ambience_ide.parsers;

import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.IntVal;

import java.util.Optional;
import java.util.regex.Pattern;

public class IntParser {
    public static final String intPattern = "[0-9]+";

    public static final Pattern intValPattern = Pattern.compile(
            intPattern + "|undefined"
    );


    public static Optional<String> validate(String value) {
        var trim = value.trim();
        return intValPattern.matcher(trim).matches() ? Optional.empty() : Optional.of("'" + trim + "' is not a valid int value.");
    }

    public static IntVal parse(String value) {
        try {
            return new IntVal(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return IntVal.UNDEFINED;
        }
    }
}
