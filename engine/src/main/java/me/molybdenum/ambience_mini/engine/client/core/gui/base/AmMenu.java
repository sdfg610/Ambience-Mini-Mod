package me.molybdenum.ambience_mini.engine.client.core.gui.base;

import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.AmWidget;

import java.util.ArrayList;
import java.util.Objects;

public abstract class AmMenu<TPoseStack>
{
    protected static final int MENU_INNER_MARGIN = 10;
    protected static final int BASE_SEPARATION = 5;

    protected final BaseDrawer<TPoseStack> drawer;
    public final McScreen screen;

    private final ArrayList<AmWidget> widgets = new ArrayList<>();
    private AmWidget focused = null;


    protected AmMenu(BaseDrawer<TPoseStack> drawer, String menuName, McScreenBuilder<TPoseStack> builder) {
        this.drawer = drawer;
        this.screen = builder.make(this, menuName);
    }

    public void open() {
        screen.open();
    }


    protected <T extends AmWidget> T addWidget(T widget) {
        widgets.add(widget);
        return widget;
    }

    protected void removeWidget(AmWidget widget) {
        widgets.remove(widget);
    }


    public void setPose(TPoseStack pose) {
        drawer.setup(pose);
    }

    public void render(int mouseX, int mouseY) {
        for (var widget : widgets)
            widget.render(drawer, mouseX, mouseY);
    }

    public boolean mousePressed(double mouseX, double mouseY, int mouseKeyIndex) {
        var handler = widgets.stream()
                .map(widget -> widget.mousePressed(drawer, mouseX, mouseY, mouseKeyIndex))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (handler != null) {
            if (focused != null)
                focused.setFocused(false);
            focused = handler;
            focused.setFocused(true);
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyIndex) {
        if (focused != null)
            return focused.keyPressed(drawer, keyIndex);
        return false;
    }

    public boolean charTyped(char ch) {
        return focused != null && focused.charTyped(ch);
    }


    public abstract void onInit();
    public abstract void onClose();
}
