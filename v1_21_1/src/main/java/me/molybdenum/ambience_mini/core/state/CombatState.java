package me.molybdenum.ambience_mini.core.state;

import me.molybdenum.ambience_mini.engine.core.setup.BaseClientConfig;
import me.molybdenum.ambience_mini.engine.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.core.state.BaseCombatState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CombatState extends BaseCombatState<Entity, Vec3>
{
    private final Minecraft mc = Minecraft.getInstance();


    public CombatState(
            BaseClientConfig config,
            PlayerState playerReader,
            LevelState levelReader,
            ServerSetup serverSetup) {
        super(config, playerReader, levelReader, serverSetup);
    }


    @Override
    public boolean isEntityDead(Entity entity) {
        return !entity.isAlive();
    }

    @Override
    public int getEntityId(Entity entity) {
        return 0;
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

        return bossMap == null
                ? List.of()
                : bossMap.values()
                .stream()
                .map(bossEvent -> ((TranslatableContents)bossEvent.getName().getContents()).getKey())
                .toList();
    }
}
