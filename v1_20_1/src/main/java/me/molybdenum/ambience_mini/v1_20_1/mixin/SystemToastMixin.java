package me.molybdenum.ambience_mini.v1_20_1.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(SystemToast.class)
public class SystemToastMixin
{
    @Shadow
    private Component title;
    @Shadow
    private List<FormattedCharSequence> messageLines;
    @Shadow
    private boolean changed;
    @Final
    @Mutable
    @Shadow
    private int width;


    // Is used in Notification.java, but through reflection.
    @Unique
    public void ambienceMini$resetMultiline(Minecraft mc, Component title, Component message) {
        Font font = mc.font;
        List<FormattedCharSequence> list = font.split(message, 200);

        this.title = title;
        this.messageLines = list;
        width = list.stream().mapToInt(font::width).max().orElse(200) + 30;
        this.changed = true;
    }
}
