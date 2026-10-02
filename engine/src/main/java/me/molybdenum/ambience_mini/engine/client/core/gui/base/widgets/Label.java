package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets;

import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

public class Label extends AmWidget
{
    String text;


    public Label(int x, int y, int width, int height, String text) {
        super(x, y, width, height);
        this.text = text;
    }


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        int textHeight = drawer.getTextHeight(text);
        drawer.drawText(textDrawer -> textDrawer.drawText(
                text,
                new Vector2i(x, y + (height-textHeight)/2),
                Color.WHITE
        ));
    }
}
