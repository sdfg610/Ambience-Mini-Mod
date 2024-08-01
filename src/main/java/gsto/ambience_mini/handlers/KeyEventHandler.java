package gsto.ambience_mini.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import gsto.ambience_mini.AmbienceMini;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AmbienceMini.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyEventHandler
{
    private static final KeyMapping reloadKey = new KeyMapping("key.reload", InputConstants.KEY_P, "key.categories.ambience_mini");

    @SubscribeEvent
    public static void keyEvent(InputEvent.Key event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isWindowActive()) {
            if (reloadKey.consumeClick()) {
                SystemToast.addOrUpdate(mc.getToasts(), SystemToast.SystemToastIds.TUTORIAL_HINT, Component.translatable("toast.reload_title"), Component.translatable("toast.reload_description"));
                AmbienceMini.tryReload();
            }
        }
    }
}
