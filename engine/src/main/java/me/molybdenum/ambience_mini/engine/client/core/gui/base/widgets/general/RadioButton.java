package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.general;

import me.molybdenum.ambience_mini.engine.client.core.gui.GuiConstants;
import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.AmWidget;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

import java.util.ArrayList;
import java.util.function.Consumer;

public class RadioButton extends AmWidget
{
    private static final int OUTER_RADIUS = 6;
    private static final int INNER_RADIUS = OUTER_RADIUS - 2;
    private static final int CORNERS = 8;
    private static final int BTN_TEXT_SEP = 3;

    private final RadioGroup group;
    private boolean selected;

    private final Label lblText;


    public RadioButton(int x, int y, String text, RadioGroup group) {
        super(x, y, 0, OUTER_RADIUS*2);

        lblText = new Label(
                x + OUTER_RADIUS*2 + BTN_TEXT_SEP,
                y + (height-tools.getLineHeight()+1)/2,
                text
        );
        this.lblText.autoSize();
        this.width = OUTER_RADIUS*2 + BTN_TEXT_SEP + lblText.getWidth();

        group.buttons.add(this);
        this.group = group;
    }


    public void setText(String text) {
        this.lblText.setText(text);
    }

    public String getText() {
        return lblText.getText();
    }


    @Override
    public void setX(int x) {
        super.setX(x);
        this.lblText.setX(x + OUTER_RADIUS*2 + BTN_TEXT_SEP);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.lblText.setY(y + (height-tools.getLineHeight()+1)/2);
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
        this.lblText.setWidth(width - OUTER_RADIUS*2 - BTN_TEXT_SEP);
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
        this.lblText.render(drawer, mouseX, mouseY);
    }

    @Override
    public AmWidget mousePressed(double mouseX, double mouseY, int mouseKeyIndex) {
        if (isMouseOver(mouseX, mouseY) && mouseKeyIndex == GuiConstants.MOUSE_LEFT) {
            tools.PlayMouseClick();
            group.resetAll();
            if (!selected)
                group.onChanged.accept(this);
            selected = true;
            return this;
        }
        return null;
    }


    public static RadioGroup newGroup() {
        return new RadioGroup(btn -> {});
    }

    public static RadioGroup newGroup(Consumer<RadioButton> onChanged) {
        return new RadioGroup(onChanged);
    }


    public static class RadioGroup {
        private final ArrayList<RadioButton> buttons = new ArrayList<>();
        private final Consumer<RadioButton> onChanged;


        public RadioGroup(Consumer<RadioButton> onChanged) {
            this.onChanged = onChanged;
        }


        private void resetAll() {
            for (var btn : buttons)
                btn.selected = false;
        }
    }
}
