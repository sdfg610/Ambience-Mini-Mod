package me.molybdenum.ambience_mini.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import me.molybdenum.ambience_mini.AmbienceMini;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AmbienceMini.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class KeyRegisterHandler
{
    public static final KeyMapping reloadKey = new KeyMapping("key.reload", InputConstants.KEY_P, "key.categories.ambience_mini");

    @SubscribeEvent
    public static void registerKeybindings(final RegisterKeyMappingsEvent event) {
        event.register(reloadKey);
    }
}
