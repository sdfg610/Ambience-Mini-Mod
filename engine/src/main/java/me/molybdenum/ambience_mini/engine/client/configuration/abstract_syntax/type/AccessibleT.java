package me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type;

import java.util.Map;

public interface AccessibleT {
    Map<String, Type> fieldTypes();

    default Type getField(String field) {
        return fieldTypes().get(field);
    }
}
