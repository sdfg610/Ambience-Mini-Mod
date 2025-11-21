package me.molybdenum.ambience_mini.engine.core.util;

import me.molybdenum.ambience_mini.engine.AmLang;

public abstract class BaseNotification<TComponent>
{
    public void showToast(AmLang key) {
        addOrUpdateToast(makeTranslatable(key.key));
    }

    public void showToast(String text) {
        addOrUpdateToast(makeLiteral(text));
    }


    public void printToChat(AmLang key) {
        printToChat(makeTranslatable(key.key));
    }

    public void printToChat(String text) {
        printToChat(makeLiteral(text));
    }


    protected abstract TComponent makeTranslatable(String key);
    protected abstract TComponent makeLiteral(String text);

    protected abstract void addOrUpdateToast(TComponent message);
    protected abstract void printToChat(TComponent message);
}
