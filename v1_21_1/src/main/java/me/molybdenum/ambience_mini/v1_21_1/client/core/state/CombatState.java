package me.molybdenum.ambience_mini.v1_21_1.client.core.state;

import me.molybdenum.ambience_mini.engine.client.core.state.BaseCombatState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CombatState extends BaseCombatState<Entity, Vec3>
{
    private final Minecraft mc = Minecraft.getInstance();


    @Override
    public boolean isEntityDead(Entity entity) {
        return !entity.isAlive();
    }

    @Override
    public int getEntityId(Entity entity) {
        return entity.getId();
    }

    @Override
    public String getEntityResourceLocation(Entity entity) {
        @SuppressWarnings("unchecked")
        MappedRegistry<EntityType<?>> registry = (MappedRegistry<EntityType<?>>)BuiltInRegistries.ENTITY_TYPE;
        var key = registry.getKey(entity.getType());
        return key == null ? null : key.toString();
    }

    @Override
    public Float getEntityHealth(Entity entity) {
        return (entity instanceof LivingEntity liv) ? liv.getHealth() : null;
    }

    @Override
    public Float getEntityMaxHealth(Entity entity) {
        return (entity instanceof LivingEntity liv) ? liv.getMaxHealth() : null;
    }


    @Override
    public boolean isCombatableEntity(Entity entity, Entity player) {
        return (entity instanceof Monster || entity instanceof NeutralMob)
                && !(entity instanceof TamableAnimal tam && player != null && player.getUUID() == tam.getOwnerUUID())
                && entity.isAlive();
    }


    @Override
    public boolean inBossFight() {
        Map<UUID, LerpingBossEvent> bossMap =
                ObfuscationReflectionHelper.getPrivateValue(BossHealthOverlay.class, mc.gui.getBossOverlay(), "events");
        return bossMap != null && !bossMap.isEmpty();
    }

    @Override
    public List<String> getBosses() {
        Map<UUID, LerpingBossEvent> bossMap =
                ObfuscationReflectionHelper.getPrivateValue(BossHealthOverlay.class, mc.gui.getBossOverlay(), "events");

        List<LerpingBossEvent> bosses = bossMap == null ? List.of() : new ArrayList<>(bossMap.values());
        return bosses.stream()
                .map(bossEvent -> extractBossKeyOrName(bossEvent.getName().getContents()))
                .toList();
    }

    private static String extractBossKeyOrName(ComponentContents component) {
        if (component instanceof TranslatableContents tComp)
            return tComp.getKey();
        else if (component instanceof PlainTextContents.LiteralContents(String text))
            return text;
        else
            return component.toString();
    }
}
