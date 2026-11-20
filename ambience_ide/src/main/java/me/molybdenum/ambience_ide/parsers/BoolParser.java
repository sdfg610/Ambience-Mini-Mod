package me.molybdenum.ambience_ide.parsers;

import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.BoolVal;

import java.util.Optional;
import java.util.regex.Pattern;

public class BoolParser {
    public static final String boolPattern = "true|false";

    public static final Pattern boolValPattern = Pattern.compile(
            boolPattern + "|undefined"
    );


    public static Optional<String> validate(String value) {
        var trim = value.trim();
        return boolValPattern.matcher(trim).matches() ? Optional.empty() : Optional.of("'" + trim + "' is not a valid bool value.");
    }

    public static BoolVal parse(String value) {
        return new BoolVal(switch (value.toLowerCase()) {
            case "true" -> true;
            case "false" -> false;
            default -> null;
        });
    }
}
