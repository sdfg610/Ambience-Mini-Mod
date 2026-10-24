package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.general;

import me.molybdenum.ambience_mini.engine.client.core.gui.GuiConstants;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.AmWidget;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

public class Button extends AmWidget
{
    private String text;
    private int textWidth, textHeight;

    private final Runnable onClick;


    public Button(int x, int y, int width, int height, String text, Runnable onClick) {
        super(x, y, width, height);
        this.onClick = onClick;
        setText(text);
    }

    public Button(String text, Runnable onClick) {
        this(0, 0,0, 0, text, onClick);
    }

    public void setText(String text) {
        this.text = text;
        this.textWidth = tools.getTextWidth(text);
        this.textHeight = tools.getTextHeight(text);
    }


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        if (isMouseOver(mouseX, mouseY))
            renderRectangle(drawer, x, y, width, height, Color.WHITE_128);
        renderBorder(drawer, x, y, width, height, 1);

        drawer.drawText(textDrawer -> textDrawer.drawText(
                text,
                new Vector2i(x + (width-textWidth)/2, y + (height-textHeight)/2),
                Color.WHITE
        ));
    }

    @Override
    public AmWidget mousePressed(double mouseX, double mouseY, int mouseKeyIndex) {
        if (isMouseOver(mouseX, mouseY) && mouseKeyIndex == GuiConstants.MOUSE_LEFT) {
            tools.PlayMouseClick();
            onClick.run();
            return this;
        }
        return null;
    }

    public void autoSize(int paddingHorizontal, int paddingVertical) {
        width = textWidth+paddingHorizontal*2;
        height = textHeight+paddingVertical*2;
    }
}
