package me.molybdenum.ambience_ide.parsers;

import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.StringVal;

import java.util.Optional;
import java.util.regex.Pattern;

public class StringParser {
    public static final String stringPattern = "(\"[^\"\\n\\r]*\")";

    public static final Pattern stringValPattern = Pattern.compile(
            stringPattern + "|undefined"
    );


    public static Optional<String> validateVal(String value) {
        var trim = value.trim();
        return stringValPattern.matcher(trim).matches() ? Optional.empty() : Optional.of("'" + trim + "' is not a valid string value.");
    }

    public static StringVal parseVal(String value) {
        return new StringVal(
                value.startsWith("\"") ? value.substring(1, value.length()-1) : null
        );
    }

    public static StringVal parseRaw(String value) {
        return new StringVal("undefined".equals(value) ? null : value);
    }
}
