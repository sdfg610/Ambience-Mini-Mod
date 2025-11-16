package me.molybdenum.ambience_mini.engine.player.music_picker.condition;

import me.molybdenum.ambience_mini.engine.player.music_picker.VarEnv;
import me.molybdenum.ambience_mini.engine.state.providers.Property;

import java.util.function.Supplier;

public class PropertyCondition implements Condition {
    Supplier<Object> _getValue;

    public PropertyCondition(Property property)  {
        _getValue = property::getValue;
    }

    @Override
    public Object evaluate(VarEnv env) {
        return _getValue.get();
    }
}
