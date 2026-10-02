package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets;

import me.molybdenum.ambience_mini.engine.client.core.gui.GuiConstants;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.utils.Utils;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

public class Textbox extends AmWidget
{
    private static final int PADDING_HORIZONTAL = 4;
    private static final int PADDING_VERTICAL = 2;

    private BaseDrawer<?> baseDrawer;

    private int displayPos = 0;
    private int cursorPos = 0;
    private long latestInteraction = 0;

    private int maxLength = 50;
    private String value = "initial value asdh ui fiuhfiuw dhfuwh di wiufh wiudfi wdif wdifhi iuhioii h";


    public Textbox(int x, int y, int width, int height) {
        super(x, y, width, height);
    }


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        baseDrawer = drawer;
        renderBorder(drawer, x, y, width, height, 1, isFocused() ? Color.WHITE : Color.OFF_WHITE);

        String str = value.substring(displayPos);

        int textX = x + 4;
        int textY = y + (height - drawer.getLineHeight()) / 2;

        drawer.withScissor(x+ PADDING_HORIZONTAL, y+PADDING_VERTICAL, width- PADDING_HORIZONTAL*2, height-PADDING_VERTICAL*2, () -> {
            drawer.drawText(textDrawer -> textDrawer.drawText(str, new Vector2i(textX, textY), Color.WHITE));
        });

        if (isFocused()) {
            boolean cursorInBounds = cursorPos >= 0 && cursorPos <= value.length();
            int relativeCursorPos = cursorPos - displayPos;

            if (cursorInBounds && (System.currentTimeMillis() - latestInteraction) % 1000 <= 500) {
                String subStr = str.substring(0, relativeCursorPos);
                var topLeft = new Vector2i(textX + drawer.getTextWidth(subStr) - 1, textY - 1);
                drawer.drawQuads(quadDrawer -> quadDrawer.draw2dRectangle(topLeft, topLeft.offset(1, drawer.getLineHeight() + 2), Color.WHITE));
            }
        }
    }

    @Override
    public AmWidget mousePressed(BaseDrawer<?> drawer, double mouseX, double mouseY, int mouseKeyIndex) {
        if (isMouseOver(mouseX, mouseY, PADDING_HORIZONTAL, PADDING_VERTICAL) && mouseKeyIndex == GuiConstants.MOUSE_LEFT) {
            int i = Utils.floor(mouseX) - x;

            setCursorPosition(drawer.headSubstringByWidth(this.value.substring(this.displayPos), i).length() + this.displayPos);
            return this;
        }
        return null;
    }

    @Override
    public boolean keyPressed(BaseDrawer<?> drawer, int keyIndex) {
        if (isFocused()) {
            switch (keyIndex) {
                case 259: // Backspace
                    deleteChar(cursorPos-1);
                    setCursorPosition(cursorPos-1);
                    break;
                case 261: // Delete
                    deleteChar(cursorPos);
                    break;
                case 262: // Right arrow
                    setCursorPosition(cursorPos+1);
                    break;
                case 263: // Left arrow
                    setCursorPosition(cursorPos-1);
                    break;
                default:
                    return false;
            }
            latestInteraction = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char ch) {
        if (isEnabled() && isFocused() && isAllowedChatCharacter(ch)) {
            insertText(Character.toString(ch));
            latestInteraction = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }
    }

    public String getText() {
        return value;
    }

    public void setText(String text) {
        value = text.length() > maxLength ? value.substring(0, maxLength) : text;
    }

    public void insertText(String str) {
        if (value.length() < maxLength) {
            var filterStr = filterText(str);
            setText(new StringBuilder(value).insert(cursorPos, filterStr).toString());
            setCursorPosition(cursorPos + filterStr.length());
        }
    }

    public void deleteChar(int pos) {
        if (pos >= 0 && pos < value.length())
            setText(new StringBuilder(value).deleteCharAt(pos).toString());
    }

    public void setCursorPosition(int newPos) {
        cursorPos = Utils.clamp(newPos, 0, value.length());
        scrollTo(cursorPos);
    }

    private void scrollTo(int pos) {
        displayPos = Math.min(displayPos, value.length());
        int i = width - PADDING_HORIZONTAL*2; // Get inner width
        String s = baseDrawer.headSubstringByWidth(value.substring(displayPos), i);
        int j = s.length() + displayPos;
        if (pos == displayPos) {
            displayPos = displayPos - baseDrawer.tailSubstringByWidth(value, i).length();
        }

        if (pos > j) {
            displayPos += pos - j;
        } else if (pos <= displayPos) {
            displayPos = displayPos - (displayPos - pos);
        }

        displayPos = Utils.clamp(displayPos, 0, value.length());
    }


    public static boolean isAllowedChatCharacter(char ch) {
        return ch != 167 && ch >= ' ' && ch != 127;
    }

    public static String filterText(String str) {
        StringBuilder stringbuilder = new StringBuilder();

        for (char c0 : str.toCharArray()) {
            if (isAllowedChatCharacter(c0)) {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }
}
