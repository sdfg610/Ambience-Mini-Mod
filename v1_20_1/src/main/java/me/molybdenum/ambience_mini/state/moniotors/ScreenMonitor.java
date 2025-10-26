package me.molybdenum.ambience_mini.state.moniotors;

import me.molybdenum.ambience_mini.engine.state.monitors.BaseScreenMonitor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ScreenMonitor extends BaseScreenMonitor
{
    private static final String OBF_MAP_BOSS_INFO = "f_93699_";

    private final Minecraft mc = Minecraft.getInstance();


    @Override
    public boolean isActualScreenNull() {
        return mc.screen == null;
    }

    @Override
    public Optional<String> getBossIdIfInFight() {
        var bossOverlay = mc.gui.getBossOverlay();
        Map<UUID, LerpingBossEvent> bossMap = ObfuscationReflectionHelper.getPrivateValue(BossHealthOverlay.class, bossOverlay, OBF_MAP_BOSS_INFO);
        if (bossMap == null || bossMap.isEmpty())
            return Optional.empty();

        var bossEvent = bossMap.values()
                .stream()
                .findFirst()
                .get();
        return Optional.of(
                ((TranslatableContents)bossEvent.getName()).getKey()
        );
    }
}
