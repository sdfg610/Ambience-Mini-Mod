package me.molybdenum.ambience_mini.v1_18_2.client.core.setup;

import com.mojang.blaze3d.platform.InputConstants;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseKeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;

public class KeyBindings extends BaseKeyBindings<KeyMapping>
{
    public KeyBindings() {
        registerKeys();
    }


    @Override
    protected int keyP() {
        return InputConstants.KEY_P;
    }

    @Override
    protected int keyEnd() {
        return InputConstants.KEY_END;
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
    protected int keyHome() {
        return InputConstants.KEY_HOME;
    }

    @Override
    protected int keyInsert() {
        return InputConstants.KEY_INSERT;
    }

    @Override
    protected int keyDelete() {
        return InputConstants.KEY_DELETE;
    }


    @Override
    protected KeyMapping createAndRegister(AmLang description, int defaultKey) {
        KeyMapping key = new KeyMapping(description.key, defaultKey, "mod_name");
        ClientRegistry.registerKeyBinding(key);
        return key;
    }

    @Override
    protected boolean isClicked(KeyMapping binding) {
        return binding.consumeClick();
    }

    @Override
    public String getKeyString(KeyMapping keyMapping) {
        return keyMapping.getTranslatedKeyMessage().getString().toUpperCase();
    }
}
