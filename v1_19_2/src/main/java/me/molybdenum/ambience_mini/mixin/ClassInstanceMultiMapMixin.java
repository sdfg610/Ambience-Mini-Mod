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
    @Final
    @Shadow
    private Map<Class<?>, List<T>> byClass = Maps.newConcurrentMap();
}
