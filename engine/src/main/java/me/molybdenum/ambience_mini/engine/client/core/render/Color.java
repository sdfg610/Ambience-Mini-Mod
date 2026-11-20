package me.molybdenum.ambience_mini.engine.client.core.render;

public class Color
{
    public static final int ALPHA_OPAQUE = 255;

    // Standard colors
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(255, 255, 255);

    public static final Color BLACK_128 = BLACK.withAlpha(ALPHA_OPAQUE / 2);
    public static final Color WHITE_128 = WHITE.withAlpha(ALPHA_OPAQUE / 2);

    public static final Color OFF_WHITE = new Color(210, 210, 210);

    // Area colors
    public static final Color AREA_LOOKING = new Color(178, 178, 255);
    public static final Color AREA_OWNED = new Color(178, 178, 255);
    public static final Color AREA_NON_OWNED_SHARED = new Color(178, 178, 255);
    public static final Color AREA_PUBLIC = new Color(178, 178, 255);

    public static final Color EXTENSION_VALID = new Color(144, 238, 144);
    public static final Color EXTENSION_ERROR = new Color(204, 2, 2);

    public final short r, g, b, a;


    public Color(int r, int g, int b, int a) {
        this.r = (short)r;
        this.g = (short)g;
        this.b = (short)b;
        this.a = (short)a;
    }

    public Color(int r, int g, int b) {
        this(r, g, b, ALPHA_OPAQUE);
    }


    public Color withAlpha(int newAlpha) {
        return new Color(r, g, b, newAlpha);
    }


    public int toABGR32() {
        return a << 24 | b << 16 | g << 8 | r;
    }
}
