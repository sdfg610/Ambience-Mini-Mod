package me.molybdenum.ambience_mini.v1_19_2.client.core.util;

import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds.TUTORIAL_HINT;

public class Notification extends BaseNotification<Component>
{
    private static final Component title = Component.translatable("mod_name");
    private static final Minecraft mc = Minecraft.getInstance();

    // I know! One is supposed to make an interface that exposes new mixin members, make the mixin class inherit
    // from this interface, and then cast to the interface when accessing the mixin class members.
    // However! The class-loader just deadlocks when SystemToastMixin inherits from an interface! WHY!? JUST WHY!?
    private static final Method resetMultiline = ObfuscationReflectionHelper.findMethod(
            SystemToast.class,
            "ambienceMini$resetMultiline",
            Minecraft.class, Component.class, Component.class
    );


    @Override
    protected Component makeTranslatable(String key, Object... arguments) {
        return Component.translatable(key, arguments);
    }

    @Override
    protected Component makeLiteral(String text) {
        return Component.literal(text);
    }

    @Override
    protected void addToast(Component message) {
        mc.execute(() -> {
            SystemToast systemtoast = mc.getToasts().getToast(SystemToast.class, TUTORIAL_HINT);
            try {
                if (systemtoast == null) {
                    var toast = SystemToast.multiline(mc, TUTORIAL_HINT, title, message);
                    resetMultiline.invoke(toast, mc, title, message);
                    mc.getToasts().addToast(toast);
                }
                else
                    resetMultiline.invoke(systemtoast, mc, title, message);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected void printToChat(Component message) {
        mc.gui.getChat().addMessage(message);
    }

    @Override
    public String translateFromKey(AmLang key) {
        return Language.getInstance().getOrDefault(key.key);
    }
}
