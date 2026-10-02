package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets;

import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

public abstract class AmWidget
{
    public int x;
    public int y;
    public int width;
    public int height;

    private boolean focused;
    private boolean enabled = true;


    public AmWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;

        this.width = width;
        this.height = height;
    }


    public int right() {
        return x+width;
    }

    public int bottom() {
        return y+height;
    }


    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isFocused() {
        return focused;
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }


    public abstract void render(BaseDrawer<?> drawer, int mouseX, int mouseY);

    public AmWidget mousePressed(BaseDrawer<?> drawer, double mouseX, double mouseY, int mouseKeyIndex) { return null; }
    public boolean keyPressed(BaseDrawer<?> drawer, int keyIndex) { return false; }
    public boolean charTyped(char ch) { return false; }


    //------------------------------------------------------------------------------------------------------------------
    // Geometry stuff
    public boolean isMouseOver(double mouseX, double mouseY) {
        return x <= mouseX && mouseX < x+width && y <= mouseY && mouseY < y+height;
    }

    public boolean isMouseOver(double mouseX, double mouseY, int paddingX, int paddingY) {
        return x+paddingX <= mouseX && mouseX < x+width-paddingX && y+paddingY <= mouseY && mouseY < y+height-paddingY;
    }


    //------------------------------------------------------------------------------------------------------------------
    // Reusable render functions
    public static void renderBorder(BaseDrawer<?> drawer, int x, int y, int width, int height, int borderThickness) {
        renderBorder(drawer, x, y, width, height, borderThickness, Color.OFF_WHITE);
    }

    public static void renderBorder(BaseDrawer<?> drawer, int x, int y, int width, int height, int borderThickness, Color color)
    {
        if (color.a <= 0)
            return;

        Vector2i tlOuter = new Vector2i(x, y);
        Vector2i trOuter = new Vector2i(x+width, y);
        Vector2i blOuter = new Vector2i(x, y+height);
        Vector2i brOuter = new Vector2i(x+width, y+height);

        Vector2i tlInner = tlOuter.offset(borderThickness, borderThickness);
        Vector2i trInner = trOuter.offset(-borderThickness, borderThickness);
        Vector2i blInner = blOuter.offset(borderThickness, -borderThickness);
        Vector2i brInner = brOuter.offset(-borderThickness, -borderThickness);

        drawer.drawQuads(quadDrawer -> {
            quadDrawer.draw2dQuad(tlOuter, tlInner, trInner, trOuter, color); // top border
            quadDrawer.draw2dQuad(tlOuter, tlInner, blInner, blOuter, color); // left border
            quadDrawer.draw2dQuad(trOuter, trInner, brInner, brOuter, color); // right order
            quadDrawer.draw2dQuad(blOuter, blInner, brInner, brOuter, color); // bottom order
        });
    }

    public static void renderRectangle(BaseDrawer<?> drawer, int x, int y, int width, int height, Color color)
    {
        if (color.a <= 0)
            return;

        Vector2i p1 = new Vector2i(x, y);
        Vector2i p2 = new Vector2i(x+width, y);
        Vector2i p3 = new Vector2i(x+width, y+height);
        Vector2i p4 = new Vector2i(x, y+height);

        drawer.drawQuads(quadDrawer -> quadDrawer.draw2dQuad(p1, p2, p3, p4, color));
    }

    public static void renderRing(BaseDrawer<?> drawer, int x, int y, int outerRadius, int innerRadius, int corners, Color color)
    {
        drawer.drawQuads(quadDrawer -> {
            int trueCorners = Math.max(3, Math.min(360, corners));
            var increment = Math.PI*2 / trueCorners;
            var center = new Vector2i(x, y);

            for (int i = 0; i < trueCorners; ++i) {
                var angle = increment*i;
                var angle1 = angle + increment;
                var p1 = center.offset((int)Math.round(Math.cos(angle)*outerRadius), (int)Math.round(Math.sin(angle)*outerRadius));
                var p2 = center.offset((int)Math.round(Math.cos(angle)*innerRadius), (int)Math.round(Math.sin(angle)*innerRadius));
                var p3 = center.offset((int)Math.round(Math.cos(angle1)*innerRadius), (int)Math.round(Math.sin(angle1)*innerRadius));
                var p4 = center.offset((int)Math.round(Math.cos(angle1)*outerRadius), (int)Math.round(Math.sin(angle1)*outerRadius));

                quadDrawer.draw2dQuad(p1, p2, p3, p4, color);
            }
        });
    }

    public static void renderCircle(BaseDrawer<?> drawer, int x, int y, int radius, int corners, Color color)
    {
        drawer.drawQuads(quadDrawer -> {
            int trueCorners = Math.max(3, Math.min(360, corners));
            var increment = Math.PI*2 / trueCorners;
            var center = new Vector2i(x, y);

            for (int i = 0; i < trueCorners; ++i) {
                var angle = increment*i;
                var angle1 = angle + increment;
                var p1 = center.offset((int)Math.round(Math.cos(angle)*radius), (int)Math.round(Math.sin(angle)*radius));
                var p2 = center.offset((int)Math.round(Math.cos(angle1)*radius), (int)Math.round(Math.sin(angle1)*radius));

                quadDrawer.draw2dQuad(p1, p2, center, center, color);
            }
        });
    }
}
