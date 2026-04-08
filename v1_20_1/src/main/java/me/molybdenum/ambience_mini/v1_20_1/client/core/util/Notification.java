package me.molybdenum.ambience_mini.v1_20_1.client.core.util;

import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.AmLang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import static net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds.TUTORIAL_HINT;

public class Notification extends BaseNotification<Component>
{
    private static final Component title = Component.translatable("mod_name");
    private static final Minecraft mc = Minecraft.getInstance();


    @Override
    protected Component makeTranslatable(String key, Object... arguments) {
        return Component.translatable(key, arguments);
    }

    @Override
    protected Component makeLiteral(String text) {
        return Component.literal(text);
    }


    @Override
    protected void addOrUpdateToast(Component message) {
        SystemToast systemtoast = mc.getToasts().getToast(SystemToast.class, TUTORIAL_HINT);
        if (systemtoast == null) {
            mc.getToasts().addToast(SystemToast.multiline(
                    Minecraft.getInstance(), TUTORIAL_HINT, title, message
            ));
        } else {
            systemtoast.reset(title, message);
        }
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
