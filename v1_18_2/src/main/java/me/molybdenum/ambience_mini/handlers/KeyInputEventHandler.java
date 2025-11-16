package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.ToastUtil;
import me.molybdenum.ambience_mini.engine.AmLang;
import me.molybdenum.ambience_mini.engine.Common;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;
import java.text.NumberFormat;

@Mod.EventBusSubscriber(modid = Common.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class KeyInputEventHandler
{
    private static final NumberFormat formatter = new DecimalFormat("#0.00000");

    @SubscribeEvent
    public static void keyEvent(final InputEvent.KeyInputEvent event)
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

        if (AmbienceMini.keyBindings.printAll.consumeClick() && AmbienceMini.levelReader.notNull() && AmbienceMini.playerReader.notNull()) {
            ToastUtil.translatable(AmLang.TOAST_PRINTING_ALL);
            AmbienceMini.LOGGER.info("All current Ambience Mini state:\n{}", AmbienceMini.gameStateProvider.readAll());
        }
    }
}
