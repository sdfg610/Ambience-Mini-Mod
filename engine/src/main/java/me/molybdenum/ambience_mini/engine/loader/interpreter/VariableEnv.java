package me.molybdenum.ambience_mini.engine.loader.interpreter;

import java.util.HashMap;

public class VariableEnv
{
    private final VariableEnv parent;
    private final HashMap<String, Object> bindings = new HashMap<>();


    private VariableEnv() {
        this.parent = null;
    }

    private VariableEnv(VariableEnv parent) {
        this.parent = parent;
    }


    public VariableEnv enterScope() {
        return new VariableEnv(this);
    }

    public VariableEnv bind(String ident, Object value) {
        if (bindings.put(ident, value) != null)
            throw new RuntimeException("Rebound ident '" + ident + "' during evaluation. The semantic analysis should have prevented this...");
        return this;
    }

    public <T> T lookup(String ident) {
        Object val = bindings.get(ident);

        if (val != null)
            //noinspection unchecked
            return (T)val;
        else if (parent != null)
            return parent.lookup(ident);

        throw new RuntimeException("Identifier '" + ident + "' could not be found during evaluation. The semantic analysis should have prevented this...");
    }


    public static VariableEnv empty() {
        return new VariableEnv();
    }
}
