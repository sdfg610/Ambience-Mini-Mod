package me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.main_menu;

import me.molybdenum.ambience_mini.engine.client.core.gui.base.widgets.AmWidget;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.QuadDrawer;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;

public class MusicBar extends AmWidget
{
    private static final int TIME_SPACING = 3;

    private int sampleRate;  // Samples per second

    private long totalTimeSeconds;
    private String totalTimeString;
    private int totalTimeWidth;

    private long currentTimeSeconds;
    private String currentTimeString;
    private int currentTimeWidth;

    private long loopStartSecond;
    private long loopEndSecond;


    public MusicBar(int x, int y, int width, int height) {
        super(x, y, width, height);
        reset();
    }


    public void reset() {
        sampleRate = -1;
        totalTimeSeconds = currentTimeSeconds = -1;
        totalTimeString = currentTimeString = "--:--";
        totalTimeWidth = currentTimeWidth = tools.getTextWidth("--:--") + TIME_SPACING;
        loopStartSecond = loopEndSecond = -1;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setTotalTime(long totalSamples) {
        if (totalSamples > 0) {
            totalTimeSeconds = totalSamples / sampleRate;
            long secondsPart = totalTimeSeconds % 60;
            long totalMinutes = totalTimeSeconds / 60;

            this.totalTimeString = String.format("%02d", totalMinutes) + ":" + String.format("%02d", secondsPart);
            this.totalTimeWidth = tools.getTextWidth(totalTimeString) + TIME_SPACING;
        }
    }

    public void setCurrentTime(long currentSamples) {
        long newTimeSeconds = currentSamples/sampleRate;
        if (newTimeSeconds == currentTimeSeconds)
            return;

        currentTimeSeconds = newTimeSeconds;
        long secondsPart = currentTimeSeconds % 60;
        long totalMinutes = currentTimeSeconds / 60;

        this.currentTimeString = String.format("%02d", totalMinutes) + ":" + String.format("%02d", secondsPart);
        this.currentTimeWidth = tools.getTextWidth(currentTimeString) + TIME_SPACING;
    }

    public void setLoopPoints(long startSample, long endSample) {
        loopStartSecond = startSample/sampleRate;
        loopEndSecond = endSample/sampleRate;
    }

    public long getTotalTimeSeconds() {
        return totalTimeSeconds;
    }


    @Override
    public void render(BaseDrawer<?> drawer, int mouseX, int mouseY) {
        int barWidth = width - totalTimeWidth - currentTimeWidth;
        int barX = x + currentTimeWidth;

        var xw = barX+barWidth;
        var yh = y+height;

        var innerBarWidth = barWidth - 2;

        drawer.drawQuads(quadDrawer -> {
            quadDrawer.draw2dRectangle(new Vector2i(barX, y+1), new Vector2i(barX+1, yh-1), Color.OFF_WHITE);
            quadDrawer.draw2dRectangle(new Vector2i(xw-1, y+1), new Vector2i(xw, yh-1), Color.OFF_WHITE);
            quadDrawer.draw2dRectangle(new Vector2i(barX+1, y+height/2), new Vector2i(xw-1, y+height/2+1), Color.OFF_WHITE);

            if (totalTimeSeconds > 0 && currentTimeSeconds >= 0)
                drawUpperPointer(
                        quadDrawer,
                        barX+1 + (int)(innerBarWidth*(currentTimeSeconds/(double)totalTimeSeconds)),
                        y+height/2
                );

            if (loopStartSecond >= 0 && loopEndSecond >= 0) {
                var pointerY = y+height/2+1;
                drawLowerPointer(
                        quadDrawer,
                        barX+1 + (int)(innerBarWidth*(loopStartSecond/(double)totalTimeSeconds)),
                        pointerY
                );
                drawLowerPointer(
                        quadDrawer,
                        barX+1 + (int)(innerBarWidth*(loopEndSecond/(double)totalTimeSeconds)),
                        pointerY
                );
            }
        });

        drawer.drawText(textDrawer -> {
            textDrawer.drawText(currentTimeString, new Vector2i(x, y + (height-tools.getLineHeight() + 1)/2), Color.WHITE);
            textDrawer.drawText(totalTimeString, new Vector2i(xw+TIME_SPACING, y + (height-tools.getLineHeight() + 1)/2), Color.WHITE);
        });
    }


    private static void drawUpperPointer(QuadDrawer quadDrawer, int pointerX, int pointerY) {
        var pointerBottom = new Vector2i(pointerX, pointerY);
        quadDrawer.draw2dQuad(
                pointerBottom,
                pointerBottom,
                pointerBottom.offset(-2,-4),
                pointerBottom.offset(2,-4),
                Color.WHITE
        );
    }

    private static void drawLowerPointer(QuadDrawer quadDrawer, int pointerX, int pointerY) {
        var pointerBottom = new Vector2i(pointerX, pointerY);
        quadDrawer.draw2dQuad(
                pointerBottom,
                pointerBottom,
                pointerBottom.offset(-2,4),
                pointerBottom.offset(2,4),
                Color.WHITE
        );
    }
}
