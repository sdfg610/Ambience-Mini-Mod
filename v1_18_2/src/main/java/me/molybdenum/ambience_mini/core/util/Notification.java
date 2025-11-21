package me.molybdenum.ambience_mini.core.util;

import me.molybdenum.ambience_mini.engine.AmLang;
import me.molybdenum.ambience_mini.engine.core.util.BaseNotification;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import static net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds.TUTORIAL_HINT;

public class Notification extends BaseNotification<Component>
{
    private static final Component title = new TranslatableComponent("mod_name");
    private static final Minecraft mc = Minecraft.getInstance();


    @Override
    protected Component makeTranslatable(String key) {
        return new TranslatableComponent(key);
    }

    @Override
    protected Component makeLiteral(String text) {
        return new TextComponent(text);
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
}
