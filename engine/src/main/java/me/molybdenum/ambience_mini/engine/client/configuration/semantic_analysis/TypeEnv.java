package me.molybdenum.ambience_mini.engine.client.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.client.configuration.abstract_syntax.type.Type;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

public class TypeEnv {
    private final LinkedList<HashMap<String, Type>> scopes = new LinkedList<>() {{
        addFirst(new HashMap<>()); // The initial, outermost scope
    }};


    public void openScope() {
        scopes.addFirst(new HashMap<>());
    }

    public void closeScope() {
        scopes.removeFirst();
    }


    /// If "ident" is undefined in the current scope, register "ident" with type "type" and return true.
    /// Otherwise, return false.
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean bind(String ident, Type type) {
        var scope = scopes.getFirst();
        if (scope.containsKey(ident))
            return false;
        scope.put(ident, type);
        return true;
    }

    public Optional<Type> lookup(String ident) {
        for (var scope : scopes) {
            var binding = scope.get(ident);
            if (binding != null)
                return Optional.of(binding);
        }
        return Optional.empty();
    }
}
