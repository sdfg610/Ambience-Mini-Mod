package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.general;

import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.AmWidget;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;

import java.util.ArrayList;


public class ScrollPanel extends AmWidget
{
    private static final int SCROLLBAR_WIDTH = 9;
    private static final int DOT_SIDE_LENGTH = 4;

    private final ArrayList<AmWidget> widgets = new ArrayList<>();

    private int contentHeight = 0;
    private int barHeight = 0;

    private int maxOffset = 0;
    private int offset = 0;

    private boolean isDragging;
    private int dragStartMouseY;
    private int dragStartBarY;


    public ScrollPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        refreshScroll();
    }


    public <T extends AmWidget> T addWidget(T widget) {
        widgets.add(widget);
        return widget;
    }

    public void clearWidgets() {
        widgets.clear();
        maxOffset = contentHeight = 0;
    }

    public void refreshScroll() {
        var bottom = widgets.stream().mapToInt(AmWidget::bottom).max().orElse(0);
        var margin = widgets.stream().mapToInt(w -> w.y).min().orElse(0);
        contentHeight = Math.max(contentHeight, bottom + margin);
        maxOffset = Math.max(0, contentHeight-height);
        barHeight = (int)((height-2) * Math.min(1, contentHeight == 0 ? 1 : height/(double)contentHeight));
        barHeight = Math.max(10, barHeight);

        offset = Math.min(offset, maxOffset);
    }


    public int getUsableWidth() {
        return width - SCROLLBAR_WIDTH - 1;
    }


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        if (visible) {
            if (isDragging) {
                var diff = (mouseY-1) - dragStartMouseY;
                var targetBarY = dragStartBarY + diff;
                offset = Math.min(maxOffset, Math.max(0, getOffsetFromBarY(targetBarY)));
            }

            renderBorder(drawer, x, y, width, height, 1);
            drawer.withOffset(x, y, 0, () ->{
                renderScrollBar(drawer, mouseX-x, mouseY-y);
                drawer.withScissor(0, 0, getUsableWidth(), height, () -> {
                    drawer.withOffset(0, -offset, 0, () -> {
                        for (var widget : widgets)
                            widget.render(drawer, mouseX-x, mouseY-y+offset);
                    });
                });
            });
        }
    }

    private void renderScrollBar(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        var barX = getUsableWidth();
        var barY = getBarY();

        renderRectangle(drawer, barX, 0, 1, height, Color.OFF_WHITE);
        renderRectangle(drawer, barX, barY, SCROLLBAR_WIDTH, 1, Color.OFF_WHITE);
        renderRectangle(drawer, barX, barY+barHeight-1, SCROLLBAR_WIDTH, 1, Color.OFF_WHITE);
        renderRectangle(drawer, barX + (SCROLLBAR_WIDTH-DOT_SIDE_LENGTH+1)/2, barY + (barHeight- DOT_SIDE_LENGTH)/2, DOT_SIDE_LENGTH, DOT_SIDE_LENGTH, Color.OFF_WHITE);

        if (isDragging || isMouseOver(mouseX, mouseY, barX, barY, barX+SCROLLBAR_WIDTH, barY+barHeight-1))
            renderRectangle(drawer, barX, barY, SCROLLBAR_WIDTH, barHeight, Color.WHITE_128);
    }


    @Override
    public AmWidget mousePressed(double mouseX, double mouseY, int mouseKeyIndex) {
        if (isMouseOver(mouseX, mouseY))
            if (mouseX > x+getUsableWidth()) {
                isDragging = true;
                dragStartMouseY = (int)mouseY-1; // -1 due to border
                dragStartBarY = getBarY()-1;
                return this;
            }
            else {
                for (var widget : widgets) {
                    var handler = widget.mousePressed(mouseX - x, mouseY - y + offset, mouseKeyIndex);
                    if (handler != null)
                        return handler;
                }
            }
        return null;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseKeyIndex) {
        isDragging = false;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        if (isMouseOver(mouseX, mouseY)) {
            offset -= (int)deltaY * (height/5);
            offset = Math.max(0, Math.min(maxOffset, offset));
            return true;
        }
        return false;
    }


    private int getBarY() {
        return (int)((double)offset/maxOffset * ((height-2)-barHeight)) + 1;
    }

    private int getOffsetFromBarY(int barY) {
        var scale = ((height-2)-barHeight);
        return scale <= 0 ? 0 : (barY*maxOffset - maxOffset) / scale;
    }
}
