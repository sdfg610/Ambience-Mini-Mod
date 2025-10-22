package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.setup.KeyBindings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Common.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class KeyRegisterEventHandlers
{
    @SubscribeEvent
    public static void registerKeybindings(final RegisterKeyMappingsEvent event) {
        KeyBindings.register(event);
    }
}
