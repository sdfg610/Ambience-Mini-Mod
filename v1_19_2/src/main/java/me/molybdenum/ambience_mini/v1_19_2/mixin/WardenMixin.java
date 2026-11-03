package me.molybdenum.ambience_mini.v1_19_2.mixin;

import me.molybdenum.ambience_mini.v1_19_2.server.handlers.ServerCombatHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Warden.class)
public abstract class WardenMixin
{
    @Shadow
    public abstract Brain<Warden> getBrain();

    @Unique
    private LivingEntity ambienceMini$lastAngry = null;

    @Inject(method = "customServerAiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/warden/Warden;syncClientAngerLevel()V"))
    public void customServerAiStep(CallbackInfo ci) {
        var brain = this.getBrain();
        var newAngry = brain.getMemory(MemoryModuleType.ATTACK_TARGET)
                .or(() -> brain.getMemory(MemoryModuleType.ROAR_TARGET))
                .orElse(null);
        if (newAngry != ambienceMini$lastAngry) {
            ServerCombatHandler.handleTargetChanged(((LivingEntity)(Object)this).getId(), ambienceMini$lastAngry, newAngry);
            ambienceMini$lastAngry = newAngry;
        }
    }
}
