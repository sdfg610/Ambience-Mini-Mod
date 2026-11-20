package me.molybdenum.ambience_ide.parsers;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.*;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.helpers.ValueList;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;

import java.util.*;
import java.util.regex.Pattern;

import static me.molybdenum.ambience_ide.parsers.RecordParser.fieldAsBoolean;
import static me.molybdenum.ambience_ide.parsers.RecordParser.fieldAsInt;
import static me.molybdenum.ambience_ide.parsers.RecordParser.fieldAsFloat;
import static me.molybdenum.ambience_ide.parsers.RecordParser.fieldAsString;

public class ListParser {
    public static final Pattern stringListPattern = makeListPattern(StringParser.stringValPattern);
    public static final Pattern recordListPattern = makeListPattern(RecordParser.recordPattern);


    private static Pattern makeListPattern(Pattern elementPattern) {
        return Pattern.compile(
                "\\[\s*((" + elementPattern + ")\\s*(?:\\s*,\\s*(" + elementPattern + "))*)?\s*]"
        );
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValueOfType(String value, Type type) {
        return "undefined".equals(value)
                || (type instanceof BoolT && fieldAsBoolean(value) != null)
                || (type instanceof IntT && fieldAsInt(value) != null)
                || (type instanceof FloatT && fieldAsFloat(value) != null)
                || (type instanceof StringT && fieldAsString(value) != null);
    }

    public static List<List<Pair<String, String>>> getRecords(String value) {
        var records = new ArrayList<List<Pair<String, String>>>();

        var recordMatcher = RecordParser.recordPattern.matcher(value);
        while (recordMatcher.find())
            records.add(RecordParser.parse(recordMatcher.group()));

        return records;
    }


    // String lists
    public static Optional<String> validateStringList(String value) {
        var trim = value.trim();
        return stringListPattern.matcher(trim).matches() ? Optional.empty() : Optional.of("The given value is not a valid list of strings.");
    }

    public static ListVal parseStringList(String value) {
        var valueList = new ValueList();

        var recordMatcher = StringParser.stringValPattern.matcher(value);
        while (recordMatcher.find())
            valueList.add(StringParser.parseVal(recordMatcher.group()));

        return new ListVal(valueList);
    }


    // Area lists
    public static Optional<String> validateAreaList(String value) {
        var trim = value.trim();
        if (!recordListPattern.matcher(trim).matches())
            return Optional.of("The given value is not a valid list of areas.");

        for (var record : getRecords(trim))
            for (var field : record) {
                var type = AreaT.FIELDS.get(field.left());
                if (type == null)
                    return Optional.of("'" + field.left() + "' is not a valid field of an array.");
                else if (!isValueOfType(field.right(), type))
                    return Optional.of("The field '" + field.left() + "' of an array expects a value of type '" + type + "', but the value '" + field.right() + "' is not of that type");
            }

        return Optional.empty();
    }

    public static ListVal parseAreaList(String value) {
        var values = new ValueList();

        for (var record : getRecords(value.trim())) {
            String name = null;
            String dimension = null;
            String owner = null;
            Boolean isShared = null;
            Boolean isLocal = null;

            for (var field : record) {
                switch (field.left()) {
                    case "name" -> name = fieldAsString(field.right());
                    case "dimension" -> dimension = fieldAsString(field.right());
                    case "owner" -> owner = fieldAsString(field.right());
                    case "isShared" -> isShared = fieldAsBoolean(field.right());
                    case "isLocal" -> isLocal = fieldAsBoolean(field.right());
                }
            }

            values.add(new AreaVal(new AreaVal.AreaDescriptor(name, dimension, owner, isShared, isLocal)));
        }

        return new ListVal(values);
    }


    // Combatant lists
    public static Optional<String> validateCombatantList(String value) {
        var trim = value.trim();
        if (!recordListPattern.matcher(trim).matches())
            return Optional.of("The given value is not a valid list of combatants.");

        for (var record : getRecords(trim))
            for (var field : record) {
                var type = CombatantT.FIELDS.get(field.left());
                if (type == null)
                    return Optional.of("'" + field.left() + "' is not a valid field of a combatant.");
                else if (!isValueOfType(field.right(), type))
                    return Optional.of("The field '" + field.left() + "' of a combatant expects a value of type '" + type + "', but the value '" + field.right() + "' is not of that type");
            }

        return Optional.empty();
    }

    public static ListVal parseCombatantList(String value) {
        var values = new ValueList();

        for (var record : getRecords(value.trim())) {
            String type_id = null;
            Float health = null;
            Float max_health = null;
            Boolean isTargetingPlayer = null;
            boolean isFightingPlayer = false;
            boolean isNeutral = false;

            for (var field : record) {
                switch (field.left()) {
                    case CombatantT.FIELD_TYPE_ID -> type_id = fieldAsString(field.right());
                    case CombatantT.FIELD_HEALTH -> health = fieldAsFloat(field.right());
                    case CombatantT.FIELD_MAX_HEALTH -> max_health = fieldAsFloat(field.right());
                    case CombatantT.FIELD_TARGETING_PLAYER -> isTargetingPlayer = fieldAsBoolean(field.right());
                    case CombatantT.FIELD_FIGHTING_PLAYER -> isFightingPlayer = Boolean.TRUE.equals(fieldAsBoolean(field.right()));
                    case CombatantT.FIELD_IS_NEUTRAL -> isNeutral = Boolean.TRUE.equals(fieldAsBoolean(field.right()));
                }
            }

            values.add(new CombatantVal(new CombatantVal.CombatantDescriptor(type_id, health, max_health, isTargetingPlayer, isFightingPlayer, isNeutral)));
        }

        return new ListVal(values);
    }


    // Boss lists
    public static Optional<String> validateBossList(String value) {
        var trim = value.trim();
        if (!recordListPattern.matcher(trim).matches())
            return Optional.of("The given value is not a valid list of bosses.");

        for (var record : getRecords(trim))
            for (var field : record) {
                var type = BossT.FIELDS.get(field.left());
                if (type == null)
                    return Optional.of("'" + field.left() + "' is not a valid field of a boss.");
                else if (!isValueOfType(field.right(), type))
                    return Optional.of("The field '" + field.left() + "' of a boss expects a value of type '" + type + "', but the value '" + field.right() + "' is not of that type");
            }

        return Optional.empty();
    }

    public static ListVal parseBossList(String value) {
        var values = new ValueList();

        for (var record : getRecords(value.trim())) {
            String id = null;
            Float health_percent = null;

            for (var field : record) {
                switch (field.left()) {
                    case BossT.FIELD_ID -> id = fieldAsString(field.right());
                    case BossT.FIELD_HEALTH_PERCENT -> health_percent = fieldAsFloat(field.right());
                }
            }

            values.add(new BossVal(new BossVal.BossDescriptor(id, health_percent == null ? 0 : health_percent)));
        }

        return new ListVal(values);
    }


    // Living lists
    public static Optional<String> validateLivingList(String value) {
        var trim = value.trim();
        if (!recordListPattern.matcher(trim).matches())
            return Optional.of("The given value is not a valid list of living entities.");

        for (var record : getRecords(trim))
            for (var field : record) {
                var type = LivingT.FIELDS.get(field.left());
                if (type == null)
                    return Optional.of("'" + field.left() + "' is not a valid field of a living entity.");
                else if (!isValueOfType(field.right(), type))
                    return Optional.of("The field '" + field.left() + "' of a living entity expects a value of type '" + type + "', but the value '" + field.right() + "' is not of that type");
            }

        return Optional.empty();
    }

    public static ListVal parseLivingList(String value) {
        var values = new ValueList();

        for (var record : getRecords(value.trim())) {
            String type_id = null;
            Float health = null;
            Float max_health = null;

            for (var field : record) {
                switch (field.left()) {
                    case LivingT.FIELD_TYPE_ID -> type_id = fieldAsString(field.right());
                    case LivingT.FIELD_HEALTH -> health = fieldAsFloat(field.right());
                    case LivingT.FIELD_MAX_HEALTH -> max_health = fieldAsFloat(field.right());
                }
            }

            values.add(new LivingVal(new LivingVal.LivingDescriptor(
                    type_id,
                    health == null ? 0 : health,
                    max_health == null ? 0 : max_health
            )));
        }

        return new ListVal(values);
    }
}
