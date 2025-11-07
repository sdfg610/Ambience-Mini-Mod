package me.molybdenum.ambience_mini.state.monitors;

import me.molybdenum.ambience_mini.engine.state.monitors.BaseScreenMonitor;
import net.minecraft.client.Minecraft;

public class ScreenMonitor extends BaseScreenMonitor {
    private final Minecraft mc = Minecraft.getInstance();

    @Override
    protected boolean isCurrentScreenNull() {
        return mc.screen == null;
    }
}
