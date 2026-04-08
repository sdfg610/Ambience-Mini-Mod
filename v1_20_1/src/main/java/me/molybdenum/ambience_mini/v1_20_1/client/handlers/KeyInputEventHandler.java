package me.molybdenum.ambience_mini.v1_20_1.client.handlers;

import me.molybdenum.ambience_mini.engine.client.core.setup.BaseKeyBindings;
import me.molybdenum.ambience_mini.v1_20_1.AmbienceMini;
import me.molybdenum.ambience_mini.engine.shared.Common;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Common.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
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
