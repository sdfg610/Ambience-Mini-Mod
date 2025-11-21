package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.core.setup.KeyBindings;
import me.molybdenum.ambience_mini.engine.Common;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;


@EventBusSubscriber(modid = Common.MOD_ID, value={Dist.CLIENT})
public class KeyInputEventHandler
{
    public static KeyBindings keyBindings = null; // Used so often it might be better to cache it


    @SubscribeEvent
    public static void keyEvent(final InputEvent.Key event) {
        if (Minecraft.getInstance().isWindowActive())
            keyBindings.handleKeyInput();
    }
}
