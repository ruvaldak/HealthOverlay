package terrails.healthoverlay;

public class Color {

    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;

    public Color(float red, float green, float blue, float alpha) {
        this.red = red / 255.0F;
        this.green = green / 255.0F;
        this.blue = blue / 255.0F;
        this.alpha = alpha / 255.0F;
    }

    public Color(float red, float green, float blue) {
        this(red, green, blue, 255.0F);
    }

    public float getAlpha() {
        return alpha;
    }

    public float getBlue() {
        return blue;
    }

    public float getGreen() {
        return green;
    }

    public float getRed() {
        return red;
    }

    @Override
    public String toString() {
        return "rgba(" + red + ", " + green + ", " + blue + ", " + alpha + ")";
    }
}
