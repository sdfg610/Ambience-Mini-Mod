package me.molybdenum.ambience_mini.engine.loader.interpreter.values;

import java.util.List;

public sealed abstract class Value permits BoolVal, FloatVal, IntVal, ListVal, StringVal
{
    public boolean asBoolean() {
        if (this instanceof BoolVal boolVal)
            return boolVal.value;
        throw new RuntimeException("Tried to get value as boolean, but value was: " + this.getClass().getName());
    }

    public int asInt() {
        if (this instanceof IntVal intVal)
            return intVal.value;
        throw new RuntimeException("Tried to get value as int, but value was: " + this.getClass().getName());
    }

    public float asFloat() {
        if (this instanceof FloatVal floatVal)
            return floatVal.value;
        throw new RuntimeException("Tried to get value as float, but value was: " + this.getClass().getName());
    }

    public String asString() {
        if (this instanceof StringVal stringVal)
            return stringVal.value;
        throw new RuntimeException("Tried to get value as string, but value was: " + this.getClass().getName());
    }

    public List<Value> asList() {
        if (this instanceof ListVal listVal)
            return listVal.value;
        throw new RuntimeException("Tried to get value as list, but value was: " + this.getClass().getName());
    }

    public abstract String toString();
}
