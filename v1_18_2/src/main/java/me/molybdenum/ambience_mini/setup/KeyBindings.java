package me.molybdenum.ambience_mini.setup;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;

public class KeyBindings
{
    public static final KeyMapping reloadKey = new KeyMapping("key.reload", InputConstants.KEY_P, "key.categories.ambience_mini");

    public static void register()
    {
        ClientRegistry.registerKeyBinding(reloadKey);
    }
}
