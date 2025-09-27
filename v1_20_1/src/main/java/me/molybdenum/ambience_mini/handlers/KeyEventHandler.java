package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AmbienceMini.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class KeyEventHandler
{
    @SubscribeEvent
    public static void keyEvent(final InputEvent.Key event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isWindowActive())
            return;

        if (KeyRegisterHandler.reloadKey.consumeClick()) {
            SystemToast.addOrUpdate(mc.getToasts(), SystemToast.SystemToastIds.TUTORIAL_HINT, Component.translatable("toast.reload_title"), Component.translatable("toast.reload_description"));
            AmbienceMini.tryReload();
        }
    }
}
