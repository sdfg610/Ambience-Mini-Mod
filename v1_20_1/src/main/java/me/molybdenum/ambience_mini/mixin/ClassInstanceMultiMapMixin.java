package me.molybdenum.ambience_mini.mixin;

import com.google.common.collect.Maps;
import net.minecraft.util.ClassInstanceMultiMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(ClassInstanceMultiMap.class)
public class ClassInstanceMultiMapMixin<T> {
    /// Replace the standard hash-map with a concurrent hash-map to avoid a rare crash when the world and Ambience
    /// Mini tries to get, simultaneously and for the first time and in the same chunk, the entities in some area.
    @Final @Shadow
    private final Map<Class<?>, List<T>> byClass = Maps.newConcurrentMap();
}
