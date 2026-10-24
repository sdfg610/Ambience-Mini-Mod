package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.general;

import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.AmWidget;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

public class Label extends AmWidget
{
    private static final long HOVER_PAUSE_TIME = 1000;
    private static final long HOVER_SCROLL_TIME = 2000;
    private static final long HOVER_SCROLL_PERIOD = HOVER_SCROLL_TIME*2 + HOVER_PAUSE_TIME*2; // Scroll, pause, scroll back, pause

    private String text;
    int textHeight;
    int textWidth;

    boolean isHovering;
    long hoverStart;


    public Label(int x, int y, int width, int height, String text) {
        super(x, y, width, height);
        setText(text);
    }

    public void setText(String text) {
        this.text = text;
        this.textHeight = tools.getTextHeight(text);
        this.textWidth = tools.getTextWidth(text);
    }

    public void autoSize() {
        height = textHeight;
        width = textWidth;
    }


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        if (visible) {
            drawer.withScissor(x, y, width, height, () -> {
                drawer.drawText(textDrawer -> textDrawer.drawText(
                        text,
                        new Vector2i(x-getOffset(mouseX, mouseY), y + (height-textHeight)/2),
                        Color.WHITE
                ));
            });
        }
    }

    private int getOffset(int mouseX, int mouseY) {
        if (textWidth <= width)
            return 0;

        var offset = 0;
        var nowHovering = isMouseOver(mouseX, mouseY);
        if (nowHovering) {
            var now = System.currentTimeMillis();
            if (!isHovering)
                hoverStart = now;

            var period = (now-hoverStart) % HOVER_SCROLL_PERIOD;
            if (period < HOVER_SCROLL_TIME)
                offset = offsetOf(period);
            else if (period < HOVER_SCROLL_TIME+HOVER_PAUSE_TIME)
                offset = textWidth-width;
            else if (period < HOVER_SCROLL_TIME*2+HOVER_PAUSE_TIME)
                offset = (textWidth-width)-offsetOf(period-(HOVER_SCROLL_TIME+HOVER_PAUSE_TIME));
        }
        isHovering = nowHovering;
        return offset;
    }

    private int offsetOf(long period) {
        return (int)(((double)period/HOVER_SCROLL_TIME)*(textWidth-width));
    }
}
