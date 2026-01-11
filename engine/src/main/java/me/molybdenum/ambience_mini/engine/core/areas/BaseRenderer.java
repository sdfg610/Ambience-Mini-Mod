package me.molybdenum.ambience_mini.engine.core.areas;

import me.molybdenum.ambience_mini.engine.utils.Pair;
import me.molybdenum.ambience_mini.engine.utils.QuadConsumer;
import me.molybdenum.ambience_mini.engine.utils.Triple;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BaseRenderer<TBuilder> {
    public int red = 255;
    public int green = 255;
    public int blue = 255;


    protected abstract TBuilder setupLineBuilder();
    protected abstract TBuilder setupRectangleBuilder();
    protected abstract void renderAndTearDown(TBuilder builder);

    protected abstract void drawLine(TBuilder builder, Point first, Point last);
    protected abstract void drawRectangle(TBuilder builder, Point corner1, Point corner2, Point corner3, Point corner4);


    public void renderLines(Consumer<BiConsumer<Point, Point>> build) {
        TBuilder builder = setupLineBuilder();
        build.accept((left, right) -> drawLine(builder, left, right));
        renderAndTearDown(builder);
    }

    public void renderRectangles(Consumer<QuadConsumer<Point, Point, Point, Point>> build) {
        TBuilder builder = setupRectangleBuilder();
        build.accept((c1, c2, c3, c4) -> drawRectangle(builder, c1, c2, c3, c4));
        renderAndTearDown(builder);
    }


    public void renderArea(Area area) {
        renderBox(area.getMinimumPoint(), area.getSize());
    }

    public void renderPointerBox(Point block) {
        renderBox(block, new Point(1,1,1));
    }


    private void renderBox(Point minimum, Point size)
    {
        var pX = minimum.offsetX(size.x());
        var pY = minimum.offsetY(size.y());
        var pZ = minimum.offsetZ(size.x());
        var pXY = pX.offsetY(size.y());
        var pXZ = pX.offsetZ(size.z());
        var pYZ = pY.offsetZ(size.z());
        var pXYZ = minimum.add(size);

        // Box outlines
        renderLines(drawLine -> {
            drawLine.accept(minimum, pX);
            drawLine.accept(minimum, pY);
            drawLine.accept(minimum, pZ);

            drawLine.accept(pX, pXZ);
            drawLine.accept(pX, pXY);

            drawLine.accept(pY, pXY);
            drawLine.accept(pY, pYZ);

            drawLine.accept(pZ, pXZ);
            drawLine.accept(pZ, pYZ);

            drawLine.accept(pYZ, pXYZ);
            drawLine.accept(pXZ, pXYZ);
            drawLine.accept(pXY, pXYZ);
        });

        // Box faces
        renderRectangles(drawQuad -> {
            drawQuad.apply(minimum, pX, pXY, pY); // North
            drawQuad.apply(minimum, pZ, pYZ, pY); // West

            drawQuad.apply(pXZ, pX, pXY, pXYZ); // East
            drawQuad.apply(pXZ, pZ, pYZ, pXYZ); // South

            drawQuad.apply(pY, pXY, pXYZ, pYZ); // Top
            drawQuad.apply(minimum, pX, pXZ, pZ); // Bottom
        });
    }


    protected int getAlpha() {
        return (int)(255 * (0.7 + 0.3 * Math.sin(Math.PI/1000 * (System.currentTimeMillis() % 2000))));
    }

    protected Triple<Float, Float, Float> getNormalSize(Point first, Point last) {
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
