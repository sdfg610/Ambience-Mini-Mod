package me.molybdenum.ambience_mini.v1_21_1.client.core.gui;

import me.molybdenum.ambience_mini.engine.client.core.gui.BaseGuiSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class GuiSounds extends BaseGuiSounds
{
    @Override
    public void PlayMouseClick() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
