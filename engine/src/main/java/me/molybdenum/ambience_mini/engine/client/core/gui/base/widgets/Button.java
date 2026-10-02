package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets;

import me.molybdenum.ambience_mini.engine.client.core.gui.BaseGuiSounds;
import me.molybdenum.ambience_mini.engine.client.core.gui.GuiConstants;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

public class Button extends AmWidget
{
    public String text;
    private final Runnable onClick;


    public Button(int x, int y, int width, int height, String text, Runnable onClick) {
        super(x, y, width, height);
        this.text = text;
        this.onClick = onClick;
    }


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        if (isMouseOver(mouseX, mouseY))
            renderRectangle(drawer, x, y, width, height, Color.WHITE_128);
        renderBorder(drawer, x, y, width, height, 1);

        int textWidth = drawer.getTextWidth(text);
        int textHeight = drawer.getTextHeight(text);
        drawer.drawText(textDrawer -> textDrawer.drawText(
                text,
                new Vector2i(x + (width-textWidth)/2, y + (height-textHeight)/2),
                Color.WHITE
        ));
    }

    @Override
    public AmWidget mousePressed(BaseDrawer<?> drawer, double mouseX, double mouseY, int mouseKeyIndex) {
        if (isMouseOver(mouseX, mouseY) && mouseKeyIndex == GuiConstants.MOUSE_LEFT) {
            BaseGuiSounds.get().PlayMouseClick();
            onClick.run();
            return this;
        }
        return null;
    }
}
