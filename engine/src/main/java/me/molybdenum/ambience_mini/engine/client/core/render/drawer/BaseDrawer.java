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

    private final Stack<Offset> offsetStack = new Stack<>() {{
        add(new Offset(0f,0f,0f));
    }};

    private final Stack<Scissor> scissorStack = new Stack<>();


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
        int offX = Math.round(offset.x), offY = Math.round(offset.y);

        var scale = getGuiScale();
        int trueX = (int)Math.round((offX + x)*scale);
        int trueY = (int)Math.round((offY + y)*scale);
        int trueW = (int)Math.round(width*scale);
        int trueH = (int)Math.round(height*scale);
        var scissor = getScissorIntersection(trueX, trueY, trueW, trueH);

        scissorStack.push(scissor);
        enableScissor(scissor.x, scissor.y, scissor.width, scissor.height);
        body.run();
        disableScissor();
        scissorStack.pop();

        if (!scissorStack.isEmpty()) {
            var top = scissorStack.peek();
            enableScissor(top.x, top.y, top.width, top.height);
        }
    }

    private Scissor getScissorIntersection(int x, int y, int width, int height) {
        if (scissorStack.isEmpty())
            return new Scissor(x, y, width, height);

        var top = scissorStack.peek();
        if (top.x + top.width < x || x+width < top.x || top.y + top.height < y || y+height < top.y)
            return new Scissor(0,0,0,0);

        int trueX1 = Math.max(x, top.x);
        int trueY1 = Math.max(y, top.y);
        int trueX2 = Math.min(x+width, top.right());
        int trueY2 = Math.min(y+height, top.bottom());

        return new Scissor(trueX1, trueY1, trueX2-trueX1, trueY2-trueY1);
    }


    public void withOffset(float x, float y, float z, Runnable body) {
        pushOffset(x, y, z);
        var off = offsetStack.peek();
        offsetStack.push(new Offset(off.x+x, off.y+y, off.z+z));
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


    private record Offset(float x, float y, float z) { }

    private record Scissor(int x, int y, int width, int height) {
        public int right() {
            return x+width;
        }

        public int bottom() {
            return y+height;
        }
    }
}
