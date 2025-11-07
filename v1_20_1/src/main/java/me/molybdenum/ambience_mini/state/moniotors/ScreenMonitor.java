package me.molybdenum.ambience_mini.state.moniotors;

import me.molybdenum.ambience_mini.engine.state.monitors.BaseScreenMonitor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ScreenMonitor extends BaseScreenMonitor
{
    private final Minecraft mc = Minecraft.getInstance();

    @Override
    protected boolean isCurrentScreenNull() {
        return mc.screen == null;
    }
}
