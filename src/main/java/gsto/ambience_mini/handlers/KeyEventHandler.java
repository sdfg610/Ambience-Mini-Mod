package gsto.ambience_mini.handlers;

import com.mojang.blaze3d.platform.InputConstants;
import gsto.ambience_mini.AmbienceMini;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AmbienceMini.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyEventHandler
{
    private static final KeyMapping reloadKey = new KeyMapping("key.reload", InputConstants.KEY_P, "key.categories.ambience_mini");

    @SubscribeEvent
    public static void keyEvent(InputEvent.KeyInputEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isWindowActive()) {
            if (reloadKey.consumeClick()) {
                SystemToast.addOrUpdate(mc.getToasts(), SystemToast.SystemToastIds.TUTORIAL_HINT, new TranslatableComponent("toast.reload_title"), new TranslatableComponent("toast.reload_description"));
                AmbienceMini.tryReload();
            }
        }
    }
}
