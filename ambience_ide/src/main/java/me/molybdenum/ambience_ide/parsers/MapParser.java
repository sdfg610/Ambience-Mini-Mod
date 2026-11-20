package me.molybdenum.ambience_ide.parsers;

import me.molybdenum.ambience_mini.engine.server.core.flags.FlagManager;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.MapVal;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.StringVal;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.helpers.ValueMap;

import java.util.Optional;
import java.util.regex.Pattern;

@SuppressWarnings("SameParameterValue")
public class MapParser {
    public static final Pattern strStrElementPattern = makeElementPattern(StringParser.stringPattern, StringParser.stringValPattern.pattern());
    public static final Pattern strStrMapPattern = makeMapPattern(strStrElementPattern);


    private static Pattern makeElementPattern(String keyRegex, String valueRegex) {
        return Pattern.compile(
                keyRegex + "\\s*:\\s*" + valueRegex
        );
    }

    private static Pattern makeMapPattern(Pattern elementPattern) {
        return Pattern.compile(
                "(\\{\\s*((" + elementPattern + ")\\s*(,\\s*(" + elementPattern + ")\\s*)*)?})?"
        );
    }


    // Flag map
    public static Optional<String> validateFlagMap(String value) {
        var trim = value.trim();
        if (!strStrMapPattern.matcher(trim).matches())
            return Optional.of("The given value is not a valid map from string to string.");

        var elementMatcher = strStrElementPattern.matcher(value);
        while (elementMatcher.find())
            if (!FlagManager.validateId(elementMatcher.group(1)))
                return Optional.of("'" + elementMatcher.group(1) + "' is not a valid flag ID.");
            else if (!FlagManager.validateValue(elementMatcher.group(2)))
                return Optional.of("'" + elementMatcher.group(2) + "' is not a valid flag value.");

        return Optional.empty();
    }

    public static MapVal parseStringStringMap(String value) {
        var map = new ValueMap();

        var elementMatcher = strStrElementPattern.matcher(value);
        while (elementMatcher.find())
            map.put(new StringVal(elementMatcher.group(1)), StringParser.parseVal(elementMatcher.group(2)));

        return new MapVal(map);
    }
}
