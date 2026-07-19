package me.molybdenum.ambience_mini.v1_21_1.mixin;

import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import me.molybdenum.ambience_mini.v1_21_1.client.core.state.CombatState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LivingEntity.class)
public class LivingEntityMixin
{
    @Unique
    @OnlyIn(Dist.CLIENT)
    private static CombatState ambienceMini$combat;


    static {
        AmbienceMini.registerOnClientCoreInitListener(
                () -> ambienceMini$combat = AmbienceMini.clientCore.combatState
        );
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(at = @At("HEAD"), method = "handleDamageEvent")
    public void handleDamageEvent(DamageSource p_270229_, CallbackInfo ci) {
        ambienceMini$combat.handleInteraction((LivingEntity)(Object)this, p_270229_.getEntity());
    }
}
