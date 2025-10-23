package me.molybdenum.ambience_mini.setup;

import com.mojang.blaze3d.platform.InputConstants;
import me.molybdenum.ambience_mini.engine.setup.BaseKeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;

public class KeyBindings extends BaseKeyBindings<KeyMapping>
{
    @Override
    protected int keyP() {
        return InputConstants.KEY_P;
    }

    @Override
    protected int keyPageUp() {
        return InputConstants.KEY_PAGEUP;
    }

    @Override
    protected int keyPageDown() {
        return InputConstants.KEY_PAGEDOWN;
    }

    @Override
    protected KeyMapping makeAndRegister(String description, int defaultKey) {
        KeyMapping key = new KeyMapping(description, defaultKey, "mod_name");
        ClientRegistry.registerKeyBinding(key);
        return key;
    }
}
