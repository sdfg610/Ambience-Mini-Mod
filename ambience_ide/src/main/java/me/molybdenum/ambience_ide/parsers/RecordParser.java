package me.molybdenum.ambience_ide.parsers;

import me.molybdenum.ambience_mini.engine.shared.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class RecordParser {
    public static final Pattern fieldPattern = Pattern.compile(
            "([_a-zA-Z][_a-zA-Z0-9]*)\\s*=\\s*((?i)" + StringParser.stringPattern + "|" + IntParser.intPattern + "|" + FloatParser.floatPattern + "|" + BoolParser.boolPattern + "|undefined)"
    );
    public static final Pattern recordPattern = Pattern.compile(
            "\\{\\s*(" + fieldPattern.pattern() + ")?(?:\\s*,\\s*(" + fieldPattern.pattern() + "))*\\s*}"
    );


    public static Optional<String> validate(String value) {
        var trim = value.trim();
        return recordPattern.matcher(trim).matches() ? Optional.empty() : Optional.of("'" + trim + "' is not a valid record value.");
    }

    public static List<Pair<String, String>> parse(String value) {
        var fields = new ArrayList<Pair<String, String>>();
        var fieldMatcher = fieldPattern.matcher(value);
        while (fieldMatcher.find()) {
            var nameAndValue = fieldMatcher.group().split("=");
            fields.add(new Pair<>(nameAndValue[0], nameAndValue[1]));
        }
        return fields;
    }


    public static Boolean fieldAsBoolean(String val) {
        return switch (val.toLowerCase()) {
            case "true" -> true;
            case "false" -> false;
            default -> null;
        };
    }

    public static String fieldAsString(String val) {
        if (val.startsWith("\""))
            return val.substring(1, val.length()-1);
        return null;
    }

    public static Integer fieldAsInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public static Float fieldAsFloat(String val) {
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
