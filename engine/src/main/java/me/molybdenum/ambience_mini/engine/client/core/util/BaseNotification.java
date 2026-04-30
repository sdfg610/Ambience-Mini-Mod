package me.molybdenum.ambience_mini.engine.client.core.util;

import me.molybdenum.ambience_mini.engine.shared.AmLang;
import me.molybdenum.ambience_mini.engine.shared.utils.Text;

import java.util.Arrays;

public abstract class BaseNotification<TComponent>
{
    public void showToast(AmLang key, Object... arguments) {
        addOrUpdateToast(makeTranslatable(key.key, arguments));
    }


    public void printToChat(Text text) {
        if (text.isLiteral)
            printLiteralToChat(text.base);
        else
            printTranslatableToChat(text.base, Arrays.stream(text.args).toArray());
    }


    public void printTranslatableToChat(AmLang key, Object... arguments) {
        printTranslatableToChat(key.key, arguments);
    }

    private void printTranslatableToChat(String key, Object... arguments) {
        makeTranslatable(key, arguments);
    }


    public void printLiteralToChat(String text) {
        printToChat(makeLiteral(text));
    }


    protected abstract TComponent makeTranslatable(String key, Object... arguments);
    protected abstract TComponent makeLiteral(String text);

    protected abstract void addOrUpdateToast(TComponent message);
    protected abstract void printToChat(TComponent message);

    public abstract String translateFromKey(AmLang key);
}
