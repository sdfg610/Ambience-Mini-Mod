package me.molybdenum.ambience_mini.core.state;

import me.molybdenum.ambience_mini.engine.core.state.BaseScreenState;
import net.minecraft.client.Minecraft;

public class ScreenState extends BaseScreenState
{
    private final Minecraft mc = Minecraft.getInstance();


    @Override
    protected boolean isScreenNull()  {
        return mc.screen == null;
    }
}
