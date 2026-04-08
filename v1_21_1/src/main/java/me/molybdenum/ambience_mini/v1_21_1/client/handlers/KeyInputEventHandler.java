package me.molybdenum.ambience_mini.v1_21_1.client.handlers;

import me.molybdenum.ambience_mini.engine.client.core.setup.BaseKeyBindings;
import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;


@EventBusSubscriber(modid = Common.MOD_ID, value={Dist.CLIENT})
public class KeyInputEventHandler
{
    private static BaseKeyBindings<?> keyBindings;


    static {
        AmbienceMini.registerOnClientCoreInitListener(
                () -> keyBindings = AmbienceMini.clientCore.keyBindings
        );
    }


    @SubscribeEvent
    public static void keyEvent(final InputEvent.Key event) {
        if (Minecraft.getInstance().isWindowActive())
            keyBindings.handleKeyInput();
    }
}
