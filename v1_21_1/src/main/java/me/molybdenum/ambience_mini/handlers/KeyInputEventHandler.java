package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;

@EventBusSubscriber(modid = Common.MODID, value={Dist.CLIENT})
public class KeyInputEventHandler
{
    public static final SystemToast.SystemToastId AMBIENCE_TOAST = new SystemToast.SystemToastId();
    private static final NumberFormat formatter = new DecimalFormat("#0.00000");

    @SubscribeEvent
    public static void keyEvent(final InputEvent.Key event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isWindowActive())
            return;

        if (AmbienceMini.keyBindings.reloadKey.consumeClick()) {
            SystemToast.addOrUpdate(mc.getToasts(), AMBIENCE_TOAST, Component.translatable("mod_name"), Component.translatable("toast.reload_description"));
            AmbienceMini.tryReload();
        }

        if (AmbienceMini.keyBindings.nextMusicKey.consumeClick()) {
            SystemToast.addOrUpdate(mc.getToasts(), AMBIENCE_TOAST, Component.translatable("mod_name"), Component.translatable("toast.next_music_description"));
            AmbienceMini.ambienceThread.forceSelectNewMusic();
        }

        if (AmbienceMini.keyBindings.showCaveScore.consumeClick()) {
            if (AmbienceMini.level.notNull() && AmbienceMini.player.notNull()) {
                String valueStr = formatter.format(AmbienceMini.caveDetector.getAveragedCaveScore(AmbienceMini.level, AmbienceMini.player).orElse(0.0));
                SystemToast.addOrUpdate(mc.getToasts(), AMBIENCE_TOAST, Component.translatable("mod_name"), Component.literal("Cave score = " + valueStr));
            }
        }
    }
}
