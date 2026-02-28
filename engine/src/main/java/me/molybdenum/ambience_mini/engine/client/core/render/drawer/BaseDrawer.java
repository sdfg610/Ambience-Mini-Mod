package me.molybdenum.ambience_mini.engine.client.core.render.drawer;

import me.molybdenum.ambience_mini.engine.client.core.render.Vector2i;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.shared.areas.Vector3i;
import me.molybdenum.ambience_mini.engine.shared.utils.Triple;

import java.util.function.Consumer;

public abstract class BaseDrawer
{
    private final LineDrawer lineDrawer = BaseDrawer.this::drawLine;
    private final QuadDrawer quadDrawer = BaseDrawer.this::drawQuad;
    private final TextDrawer textDrawer = BaseDrawer.this::drawText;


    protected abstract void beginLineBuilder();
    protected abstract void drawLine(Vector3i p1, Vector3i p2, Color color, int alpha);
    protected abstract void endLineBuilder();

    protected abstract void beginQuadBuilder();
    protected abstract void drawQuad(Vector3i p1, Vector3i p2, Vector3i p3, Vector3i p4, Color color, int alpha);
    protected abstract void endQuadBuilder();

    protected abstract void beginTextBuilder();
    protected abstract void drawText(String text, Vector2i position, Color color, int alpha);
    protected abstract void endTextBuilder();

    public abstract int getTextWidth(String text);
    public abstract int getLineHeight();


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
