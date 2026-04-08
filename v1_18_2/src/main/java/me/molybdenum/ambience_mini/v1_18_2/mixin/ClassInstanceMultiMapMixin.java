package me.molybdenum.ambience_mini.v1_18_2.mixin;

import com.google.common.collect.Maps;
import net.minecraft.util.ClassInstanceMultiMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(ClassInstanceMultiMap.class)
public class ClassInstanceMultiMapMixin<T> {
    @Final
    @Shadow
    private final Map<Class<?>, List<T>> byClass = Maps.newConcurrentMap();
}
