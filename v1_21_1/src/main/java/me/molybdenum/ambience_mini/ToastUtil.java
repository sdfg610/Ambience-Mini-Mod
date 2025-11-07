package me.molybdenum.ambience_mini;

import me.molybdenum.ambience_mini.engine.AmLang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ToastUtil
{
    public static final SystemToast.SystemToastId AMBIENCE_TOAST = new SystemToast.SystemToastId();
    private static final Component title = Component.translatable("mod_name");

    private static final Minecraft mc = Minecraft.getInstance();

    public static void translatable(AmLang key) {
        addOrUpdate(Component.translatable(key.key));
    }

    public static void literal(String text) {
        addOrUpdate(Component.literal(text));
    }

    private static void addOrUpdate(Component message) {
        SystemToast systemtoast = mc.getToasts().getToast(SystemToast.class, AMBIENCE_TOAST);
        if (systemtoast == null) {
            mc.getToasts().addToast(SystemToast.multiline(
                    Minecraft.getInstance(), AMBIENCE_TOAST, title, message
            ));
        } else {
            systemtoast.reset(title, message);
        }
    }
}
