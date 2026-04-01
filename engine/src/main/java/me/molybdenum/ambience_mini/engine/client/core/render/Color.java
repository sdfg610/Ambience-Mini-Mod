package me.molybdenum.ambience_mini.engine.client.core.render;

@SuppressWarnings("ClassCanBeRecord")
public class Color
{
    // Standard colors
    public static final int ALPHA_OPAQUE = 255;
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color BLACK = new Color(0, 0, 0);

    // Area colors
    public static final Color AREA_LOOKING = new Color(178, 178, 255);
    public static final Color AREA_OWNED = new Color(178, 178, 255);
    public static final Color AREA_NON_OWNED_SHARED = new Color(178, 178, 255);
    public static final Color AREA_PUBLIC = new Color(178, 178, 255);

    public static final Color EXTENSION_VALID = new Color(144, 238, 144);
    public static final Color EXTENSION_ERROR = new Color(204, 2, 2);

    public final byte r, g, b;


    public Color(int r, int g, int b) {
        this.r = (byte)r;
        this.g = (byte)g;
        this.b = (byte)b;
    }


    public int toABGR32() {
        return toABGR32(ALPHA_OPAQUE);
    }

    public int toABGR32(int alpha) {
        return alpha << 24 | b << 16 | g << 8 | r;
    }

    public int toABGR32(float alpha) {
        return ((int)(255 * alpha)) << 24 | b << 16 | g << 8 | r;
    }
}
