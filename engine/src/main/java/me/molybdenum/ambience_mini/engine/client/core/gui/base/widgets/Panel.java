package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets;

import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;

import java.util.ArrayList;

public class Panel extends AmWidget
{
    public int borderThickness = 1;
    public Color backgroundColor = Color.TRANSPARENT;


    private final ArrayList<AmWidget> widgets = new ArrayList<>();


    public Panel(int x, int y, int width, int height) {
        super(x, y, width, height);
    }


    public <T extends AmWidget> T addWidget(T widget) {
        widgets.add(widget);
        return widget;
    }

    public void removeWidget(AmWidget widget) {
        widgets.remove(widget);
    }


    public void autoSize() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxRight = Integer.MIN_VALUE;
        int maxBottom = Integer.MIN_VALUE;

        for (var widget : widgets) {
            minX = Math.min(minX, widget.x);
            minY = Math.min(minY, widget.y);
            maxRight = Math.max(maxRight, widget.right());
            maxBottom = Math.max(maxBottom, widget.bottom());
        }

        this.width = maxRight + Math.max(0, minX);
        this.height = maxBottom + Math.max(0, minY);
    };


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        renderRectangle(drawer, x, y, width, height, backgroundColor);
        renderBorder(drawer, x, y, width, height, borderThickness);
        drawer.withOffset(x, y, 0, () ->{
            for (var widget : widgets)
                widget.render(drawer, mouseX-x, mouseY-y);
        });
    }

    @Override
    public AmWidget mousePressed(BaseDrawer<?> drawer, double mouseX, double mouseY, int mouseKeyIndex) {
        mouseX -= x;
        mouseY -= y;
        for (var widget : widgets) {
            var handler = widget.mousePressed(drawer, mouseX, mouseY, mouseKeyIndex);
            if (handler != null)
                return handler;
        }
        return null;
    }
}
