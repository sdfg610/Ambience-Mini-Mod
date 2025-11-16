package me.molybdenum.ambience_mini.engine.player.music_picker;

import java.util.HashMap;

public class VarEnv
{
    private VarEnv parent = null;
    private HashMap<String, Object> bindings = new HashMap<>();


    public VarEnv() { }

    private VarEnv(VarEnv parent) {
        this.parent = parent;
    }


    public VarEnv enterScope() {
        return new VarEnv(this);
    }

    public VarEnv bind(String ident, Object value) {
        if (bindings.put(ident, value) != null)
            throw new RuntimeException("Rebound ident '" + ident + "' during evaluation. The semantic analysis should have prevented this...");
        return this;
    }

    public Object lookup(String ident) {
        Object val = bindings.get(ident);

        if (val != null)
            return val;
        else if (parent != null)
            return parent.lookup(ident);

        throw new RuntimeException("Identifier '" + ident + "' could not be found during evaluation. The semantic analysis should have prevented this...");
    }


    public static VarEnv empty() {
        return new VarEnv();
    }
}
