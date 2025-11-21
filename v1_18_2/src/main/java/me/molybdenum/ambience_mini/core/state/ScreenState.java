package me.molybdenum.ambience_mini.core.state;

import me.molybdenum.ambience_mini.engine.core.state.BaseScreenState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScreenState extends BaseScreenState
{
    private final Minecraft mc = Minecraft.getInstance();

    @Override
    protected boolean isScreenNull() {
        return mc.screen == null;
    }
}
