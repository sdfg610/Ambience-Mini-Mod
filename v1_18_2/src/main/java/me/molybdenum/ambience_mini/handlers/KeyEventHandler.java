package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.setup.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.text.NumberFormat;

@Mod.EventBusSubscriber(modid = Common.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class KeyEventHandler
{
    private static final NumberFormat formatter = new DecimalFormat("#0.00000");

    @SubscribeEvent
    public static void keyEvent(final InputEvent.KeyInputEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isWindowActive())
            return;

        if (KeyBindings.reloadKey.consumeClick()) {
            SystemToast.addOrUpdate(mc.getToasts(), SystemToast.SystemToastIds.TUTORIAL_HINT, new TranslatableComponent("mod_name"), new TranslatableComponent("toast.reload_description"));
            AmbienceMini.tryReload();
        }

        if (KeyBindings.nextMusicKey.consumeClick()) {
            SystemToast.addOrUpdate(mc.getToasts(), SystemToast.SystemToastIds.TUTORIAL_HINT, new TranslatableComponent("mod_name"), new TranslatableComponent("toast.next_music_description"));
            AmbienceMini.ambienceThread.selectNewMusic();
        }

        if (KeyBindings.showCaveScore.consumeClick()) {
            if (AmbienceMini.level.notNull() && AmbienceMini.player.notNull()) {
                String valueStr = formatter.format(AmbienceMini.caveDetector.getAveragedCaveScore(AmbienceMini.level, AmbienceMini.player).orElse(0.0));
                SystemToast.addOrUpdate(mc.getToasts(), SystemToast.SystemToastIds.TUTORIAL_HINT, new TranslatableComponent("mod_name"), new TextComponent("Cave score = " + valueStr));
            }
        }
    }
}
