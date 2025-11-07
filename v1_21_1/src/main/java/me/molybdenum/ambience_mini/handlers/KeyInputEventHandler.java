package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.ToastUtil;
import me.molybdenum.ambience_mini.engine.AmLang;
import me.molybdenum.ambience_mini.engine.Common;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;


@EventBusSubscriber(modid = Common.MODID, value={Dist.CLIENT})
public class KeyInputEventHandler
{
    private static final NumberFormat formatter = new DecimalFormat("#0.00000");

    @SubscribeEvent
    public static void keyEvent(final InputEvent.Key event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isWindowActive())
            return;

        if (AmbienceMini.keyBindings.reloadKey.consumeClick()) {
            ToastUtil.translatable(AmLang.TOAST_RELOAD);
            AmbienceMini.tryReload();
        }

        if (AmbienceMini.keyBindings.nextMusicKey.consumeClick()) {
            ToastUtil.translatable(AmLang.TOAST_NEXT_MUSIC);
            AmbienceMini.ambienceThread.forceSelectNewMusic();
        }

        if (AmbienceMini.keyBindings.showCaveScore.consumeClick() && AmbienceMini.levelReader.notNull() && AmbienceMini.playerReader.notNull()) {
            String valueStr = formatter
                    .format(AmbienceMini.caveDetector.getAveragedCaveScore(AmbienceMini.levelReader, AmbienceMini.playerReader)
                    .orElse(0.0));
            ToastUtil.literal("Cave score = " + valueStr);
        }
    }
}
