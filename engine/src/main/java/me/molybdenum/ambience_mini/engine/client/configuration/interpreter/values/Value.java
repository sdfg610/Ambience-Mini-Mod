package me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values;

import me.molybdenum.ambience_mini.engine.client.configuration.interpreter.values.helpers.ValueList;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed abstract class Value<T> permits AreaVal, BoolVal, FloatVal, IntVal, ListVal, StringVal, UndefinedVal
{
    protected final T value;


    protected Value(T value) {
        this.value = value;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Abstract API
    public abstract String toStringInner(@NotNull T value);

    public abstract boolean equals(Value<?> other);


    // -----------------------------------------------------------------------------------------------------------------
    // Public API
    public boolean isUndefined() {
        return value == null;
    }

    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    public <V> V map(Function<T, V> body) {
        return value == null ? null : body.apply(value);
    }

    public <V> V match(Function<T, V> ifDefined, Supplier<V> ifUndefined) {
        return value == null ? ifUndefined.get() : ifDefined.apply(value);
    }

    public Value<?> mapOrUndefined(Function<T, Value<?>> body) {
        return value == null ? new UndefinedVal() : body.apply(value);
    }


    @Override
    public String toString() {
        return value == null ? "undefined" : toStringInner(value);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Conversions
    public Optional<Boolean> asBool() {
        return this instanceof BoolVal val ? Optional.ofNullable(val.value) : Optional.empty();
    }

    public <V> V mapBool(Function<Boolean, V> body) {
        return this instanceof BoolVal val ? val.map(body) : null;
    }


    public Optional<Integer> asInt() {
        return this instanceof IntVal val ? Optional.ofNullable(val.value) : Optional.empty();
    }

    public <V> V mapInt(Function<Integer, V> body) {
        return this instanceof IntVal val ? val.map(body) : null;
    }


    public Optional<Float> asFloat() {
        return this instanceof FloatVal fVal
                ? Optional.ofNullable(fVal.value)
                : (this instanceof IntVal iVal
                    ? Optional.of(iVal.value).map(Integer::floatValue)
                    : Optional.empty()
                );
    }

    public <V> V mapFloat(Function<Float, V> body) {
        return this instanceof FloatVal val
                ? val.map(body)
                : (this instanceof IntVal val
                        ? val.map(i -> body.apply(i.floatValue()))
                        : null
                );
    }


    public Optional<String> asString() {
        return this instanceof StringVal val ? Optional.ofNullable(val.value) : Optional.empty();
    }

    public <V> V mapString(Function<String, V> body) {
        return this instanceof StringVal val ? val.map(body) : null;
    }


    public Optional<ValueList> asList() {
        return this instanceof ListVal val ? Optional.ofNullable(val.value) : Optional.empty();
    }

    public <V> V mapList(Function<ValueList, V> body) {
        return this instanceof ListVal val ? val.map(body) : null;
    }
}
