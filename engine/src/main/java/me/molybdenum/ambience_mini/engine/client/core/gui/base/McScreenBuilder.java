package me.molybdenum.ambience_mini.engine.client.core.gui.base;

@FunctionalInterface
public interface McScreenBuilder<TPoseStack> {
    McScreen make(AmMenu<TPoseStack> menu, String screenName);
}
