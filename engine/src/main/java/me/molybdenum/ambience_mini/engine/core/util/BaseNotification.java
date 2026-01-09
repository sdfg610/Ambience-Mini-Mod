package me.molybdenum.ambience_mini.engine.core.util;

import me.molybdenum.ambience_mini.engine.AmLang;

public abstract class BaseNotification<TComponent>
{
    public void showToast(AmLang key, Object... arguments) {
        addOrUpdateToast(makeTranslatable(key.key, arguments));
    }

    public void showToast(String text) {
        addOrUpdateToast(makeLiteral(text));
    }


    public void printToChat(AmLang key, Object... arguments) {
        printToChat(makeTranslatable(key.key, arguments));
    }

    public void printToChat(String text) {
        printToChat(makeLiteral(text));
    }


    protected abstract TComponent makeTranslatable(String key, Object... arguments);
    protected abstract TComponent makeLiteral(String text);

    protected abstract void addOrUpdateToast(TComponent message);
    protected abstract void printToChat(TComponent message);
}
