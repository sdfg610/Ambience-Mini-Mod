package me.molybdenum.ambience_mini.v1_19_2.mixin;

import com.google.common.collect.Maps;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.UUID;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayMixin {
    @Final
    @Shadow
    final Map<UUID, LerpingBossEvent> events = Maps.newConcurrentMap();
}
