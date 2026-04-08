package me.molybdenum.ambience_mini.v1_21_1.client.core.setup;

import com.mojang.blaze3d.platform.InputConstants;
import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.client.core.setup.BaseKeyBindings;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class KeyBindings extends BaseKeyBindings<KeyMapping>
{
    private final RegisterKeyMappingsEvent event;


    public KeyBindings(RegisterKeyMappingsEvent event) {
        this.event = event;
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
        event.register(key);
        return key;
    }

    @Override
    protected boolean isClicked(KeyMapping keyMapping) {
        return keyMapping.consumeClick();
    }

    @Override
    public String getKeyString(KeyMapping keyMapping) {
        return keyMapping.getTranslatedKeyMessage().getString().toUpperCase();
    }
}
