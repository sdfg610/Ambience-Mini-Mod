package me.molybdenum.ambience_mini.engine.shared.configuration.semantic_analysis;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.type.Type;
import me.molybdenum.ambience_mini.engine.shared.utils.Pair;

import java.util.*;

public class TypeEnv {
    private final LinkedList<HashMap<String, TypeBinding>> scopes = new LinkedList<>() {{
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
    public Optional<TypeBinding> bind(String ident, Type type, int line) {
        var scope = scopes.getFirst();
        if (scope.containsKey(ident))
            return Optional.empty();

        var binding = new TypeBinding(type, line);
        scope.put(ident, binding);
        return Optional.of(binding);
    }

    public Optional<TypeBinding> lookup(String ident) {
        for (var scope : scopes) {
            var binding = scope.get(ident);
            if (binding != null)
                return Optional.of(binding);
        }
        return Optional.empty();
    }

    public List<Pair<String, Integer>> getTopScopeUnused() {
        var unused = new ArrayList<Pair<String, Integer>>();
        for (var binding : scopes.getFirst().entrySet()) {
            if (!binding.getValue().getIsUsed())
                unused.add(new Pair<>(binding.getKey(), binding.getValue().line));
        }
        return unused;
    }
}
