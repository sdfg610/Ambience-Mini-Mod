package me.molybdenum.ambience_mini.v1_18_2.client.core.state;

import me.molybdenum.ambience_mini.engine.client.core.state.BaseCombatState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CombatState extends BaseCombatState<Entity, Vec3>
{
    private static final String OBF_MAP_BOSS_INFO = "f_93699_";

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
    public boolean inBossFight() {
        Map<UUID, LerpingBossEvent> bossMap =
                ObfuscationReflectionHelper.getPrivateValue(BossHealthOverlay.class, mc.gui.getBossOverlay(), OBF_MAP_BOSS_INFO);
        return bossMap != null && !bossMap.isEmpty();
    }

    @Override
    public List<String> getBosses() {
        Map<UUID, LerpingBossEvent> bossMap =
                ObfuscationReflectionHelper.getPrivateValue(BossHealthOverlay.class, mc.gui.getBossOverlay(), OBF_MAP_BOSS_INFO);

        return bossMap == null
                ? List.of()
                : bossMap.values()
                .stream()
                .map(bossEvent -> extractBossKeyOrName(bossEvent.getName()))
                .toList();
    }

    private static String extractBossKeyOrName(Component component) {
        if (component instanceof TranslatableComponent tComp)
            return tComp.getKey();
        else if (component instanceof TextComponent tComp)
            return tComp.getText();
        else
            return component.toString();
    }
}
