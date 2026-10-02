package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets;

import me.molybdenum.ambience_mini.engine.client.core.gui.BaseGuiSounds;
import me.molybdenum.ambience_mini.engine.client.core.gui.GuiConstants;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

import java.util.ArrayList;

public class RadioButton extends AmWidget
{
    private static final int OUTER_RADIUS = 6;
    private static final int INNER_RADIUS = OUTER_RADIUS - 2;
    private static final int CORNERS = 8;
    private static final int BTN_TEXT_SEP = 3;

    private final RadioGroup group;
    private final String text;
    private boolean selected;


    public RadioButton(int x, int y, String text, RadioGroup group, BaseDrawer<?> drawer) {
        super(x, y, OUTER_RADIUS*2 + BTN_TEXT_SEP + drawer.getTextWidth(text), OUTER_RADIUS*2);
        this.text = text;

        group.buttons.add(this);
        this.group = group;
    }


    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        int centerX = x + OUTER_RADIUS;
        int centerY = y + height/2;
        if (isMouseOver(mouseX, mouseY))
            renderCircle(drawer, centerX, centerY, INNER_RADIUS, CORNERS, Color.WHITE_128);
        renderRing(drawer, centerX, centerY, OUTER_RADIUS, INNER_RADIUS, CORNERS, Color.OFF_WHITE);

        if (selected)
            renderCircle(drawer, centerX, centerY, INNER_RADIUS-1, CORNERS, Color.WHITE);

        int textHeight = drawer.getTextHeight(text);
        drawer.drawText(textDrawer -> textDrawer.drawText(
                text,
                new Vector2i(x + OUTER_RADIUS*2 + BTN_TEXT_SEP, y + (height-textHeight+1)/2),
                Color.WHITE
        ));
    }

    @Override
    public AmWidget mousePressed(BaseDrawer<?> drawer, double mouseX, double mouseY, int mouseKeyIndex) {
        if (isMouseOver(mouseX, mouseY) && mouseKeyIndex == GuiConstants.MOUSE_LEFT) {
            BaseGuiSounds.get().PlayMouseClick();
            group.resetAll();
            selected = true;
            return this;
        }
        return null;
    }


    public static RadioGroup newGroup() {
        return new RadioGroup();
    }


    public static class RadioGroup {
        private final ArrayList<RadioButton> buttons = new ArrayList<>();

        private void resetAll() {
            for (var btn : buttons)
                btn.selected = false;
        }
    }
}
