package me.molybdenum.ambience_mini.setup;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

public class KeyBindings
{
    public static final KeyMapping reloadKey = new KeyMapping("key.reload", InputConstants.KEY_P, "mod_name");
    public static final KeyMapping nextMusicKey = new KeyMapping("key.nextMusic", InputConstants.KEY_PAGEUP, "mod_name");
    public static final KeyMapping showCaveScore = new KeyMapping("key.showCaveScore", InputConstants.KEY_PAGEDOWN, "mod_name");

    public static void register(final RegisterKeyMappingsEvent event)
    {
        event.register(reloadKey);
        event.register(nextMusicKey);
        event.register(showCaveScore);
    }
}
