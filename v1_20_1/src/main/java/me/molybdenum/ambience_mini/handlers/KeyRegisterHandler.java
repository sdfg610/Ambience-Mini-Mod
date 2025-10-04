package me.molybdenum.ambience_mini.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import me.molybdenum.ambience_mini.engine.Common;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Common.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public class KeyRegisterHandler
{
    public static final KeyMapping reloadKey = new KeyMapping("key.reload", InputConstants.KEY_P, "mod_name");
    public static final KeyMapping nextMusicKey = new KeyMapping("key.nextMusic", InputConstants.KEY_PAGEUP, "mod_name");
    public static final KeyMapping showCaveScore = new KeyMapping("key.showCaveScore", InputConstants.KEY_PAGEDOWN, "mod_name");

    @SubscribeEvent
    public static void registerKeybindings(final RegisterKeyMappingsEvent event) {
        event.register(reloadKey);
        event.register(nextMusicKey);
        event.register(showCaveScore);
    }
}
