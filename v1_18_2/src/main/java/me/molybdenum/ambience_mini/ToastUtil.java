package me.molybdenum.ambience_mini;

import me.molybdenum.ambience_mini.engine.AmLang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds.TUTORIAL_HINT;

@OnlyIn(Dist.CLIENT)
public class ToastUtil
{
    private static final Component title = new TranslatableComponent("mod_name");

    private static final Minecraft mc = Minecraft.getInstance();


    public static void translatable(AmLang key) {
        addOrUpdate(new TranslatableComponent(key.key));
    }

    public static void literal(String text) {
        addOrUpdate(new TextComponent(text));
    }
    

    private static void addOrUpdate(Component message) {
        SystemToast systemtoast = mc.getToasts().getToast(SystemToast.class, TUTORIAL_HINT);
        if (systemtoast == null) {
            mc.getToasts().addToast(SystemToast.multiline(
                    Minecraft.getInstance(), TUTORIAL_HINT, title, message
            ));
        } else {
            systemtoast.reset(title, message);
        }
    }
}
