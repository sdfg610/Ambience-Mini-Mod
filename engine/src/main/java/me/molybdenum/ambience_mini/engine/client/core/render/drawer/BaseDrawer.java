package me.molybdenum.ambience_mini.engine.client.core.render.drawer;

import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
import me.molybdenum.ambience_mini.engine.shared.utils.Triple;

import java.util.Stack;
import java.util.function.Consumer;

public abstract class BaseDrawer<TPoseStack>
{
    private final LineDrawer lineDrawer = BaseDrawer.this::drawLine;
    private final QuadDrawer quadDrawer = BaseDrawer.this::drawQuad;
    private final TextDrawer textDrawer = BaseDrawer.this::drawText;

    private final Stack<Triple<Float, Float, Float>> offsetStack = new Stack<>() {{
        add(new Triple<>(0f,0f,0f));
    }};


    public abstract void setup(TPoseStack pose);

    protected abstract void beginLineBuilder();
    protected abstract void drawLine(Vector3i p1, Vector3i p2, Color color);
    protected abstract void endLineBuilder();

    protected abstract void beginQuadBuilder();
    protected abstract void drawQuad(Vector3i p1, Vector3i p2, Vector3i p3, Vector3i p4, Color color);
    protected abstract void endQuadBuilder();

    protected abstract void beginTextBuilder();
    protected abstract void drawText(String text, Vector2i position, Color color);
    protected abstract void endTextBuilder();

    public abstract int getTextWidth(String text);
    public abstract int getLineHeight();

    public abstract String headSubstringByWidth(String str, int width);
    public abstract String tailSubstringByWidth(String str, int width);

    public int getTextHeight(String text) {
        if(text == null || text.isEmpty())
            return 0;
        return getLineHeight() * (1 + (int)text.chars().filter(ch -> ch == '\n').count());
    }

    protected abstract void enableScissor(int x, int y, int width, int height);
    protected abstract void disableScissor();

    protected abstract void pushOffset(float x, float y, float z);
    protected abstract void popOffset();

    protected abstract double getGuiScale();


    public void drawLines(Consumer<LineDrawer> build) {
        beginLineBuilder();
        build.accept(lineDrawer);
        endLineBuilder();
    }

    public void drawQuads(Consumer<QuadDrawer> build) {
        beginQuadBuilder();
        build.accept(quadDrawer);
        endQuadBuilder();
    }

    public void drawText(Consumer<TextDrawer> build) {
        beginTextBuilder();
        build.accept(textDrawer);
        endTextBuilder();
    }


    public void withScissor(int x, int y, int width, int height, Runnable body) {
        var offset = offsetStack.peek();
        int offX = Math.round(offset.first()), offY = Math.round(offset.second());

        var scale = getGuiScale();
        enableScissor(
                (int)Math.round((offX + x)*scale),
                (int)Math.round((offY + y)*scale),
                (int)Math.round(width*scale),
                (int)Math.round(height*scale)
        );
        body.run();
        disableScissor();
    }


    public void withOffset(float x, float y, float z, Runnable body) {
        pushOffset(x, y, z);
        var off = offsetStack.peek();
        offsetStack.push(new Triple<>(off.first()+x, off.second()+y, off.third()+z));
        body.run();
        offsetStack.pop();
        popOffset();
    }



    protected Triple<Float, Float, Float> getNormalSize(Vector3i first, Vector3i last) {
        double xLength = last.x() - first.x();
        double yLength = last.y() - first.y();
        double zLength = last.z() - first.z();
        float distance = (float) Math.sqrt(xLength * xLength + yLength * yLength + zLength * zLength);
        xLength /= distance;
        yLength /= distance;
        zLength /= distance;
        return new Triple<>((float)xLength, (float)yLength, (float)zLength);
    }
}
