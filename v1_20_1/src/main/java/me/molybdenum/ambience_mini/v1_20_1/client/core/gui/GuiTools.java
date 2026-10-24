package me.molybdenum.ambience_mini.v1_20_1.client.core.gui;

import me.molybdenum.ambience_mini.engine.client.core.gui.BaseGuiTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class GuiTools extends BaseGuiTools
{
    private final Minecraft mc = Minecraft.getInstance();


    @Override
    public void PlayMouseClick() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }


    @Override
    public int getTextWidth(String text) {
        return mc.font.width(text);
    }

    @Override
    public int getLineHeight() {
        return mc.font.lineHeight;
    }


    @Override
    public String headSubstringByWidth(String str, int width) {
        return mc.font.plainSubstrByWidth(str, width);
    }

    @Override
    public String tailSubstringByWidth(String str, int width) {
        return mc.font.plainSubstrByWidth(str, width, true);
    }
}
