package me.molybdenum.ambience_mini.engine.shared.configuration.interpreter;

import me.molybdenum.ambience_mini.engine.shared.configuration.abstract_syntax.command.ModifierKinds;
import me.molybdenum.ambience_mini.engine.shared.configuration.interpreter.values.Value;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Optional;

public class VariableEnv
{
    private final VariableEnv parent;
    private final HashMap<String, Value<?>> bindings = new HashMap<>();
    private final EnumMap<ModifierKinds, Value<?>> modifiers = new EnumMap<>(ModifierKinds.class);


    public VariableEnv() {
        this.parent = null;
    }

    private VariableEnv(VariableEnv parent) {
        this.parent = parent;
    }


    public VariableEnv enterScope() {
        return new VariableEnv(this);
    }


    public VariableEnv bind(String ident, Value<?> value) {
        if (bindings.put(ident, value) != null)
            throw new RuntimeException("Rebound ident '" + ident + "' during evaluation. The semantic analysis should have prevented this...");
        return this;
    }

    public VariableEnv bind(ModifierKinds modifier, Value<?> value) {
        modifiers.put(modifier, value);
        return this;
    }


    public Value<?> lookup(String ident) {
        Value<?> val = bindings.get(ident);

        if (val != null)
            return val;
        else if (parent != null)
            return parent.lookup(ident);

        throw new RuntimeException("Identifier '" + ident + "' could not be found during evaluation. The semantic analysis should have prevented this...");
    }


    public Optional<Value<?>> lookup(ModifierKinds modifier) {
        Value<?> val = modifiers.get(modifier);

        if (val != null)
            return Optional.of(val);
        else if (parent != null)
            return parent.lookup(modifier);

        return Optional.empty();
    }
}
