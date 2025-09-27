package me.molybdenum.ambience_mini.engine.player.rule.condition;

import me.molybdenum.ambience_mini.engine.state.Property;

import java.util.function.Supplier;

public class PropertyCondition implements Condition {
    Supplier<Object> _getValue;

    public PropertyCondition(Property property)  {
        _getValue = property::getValue;
    }

    @Override
    public Object evaluate() {
        return _getValue.get();
    }
}
