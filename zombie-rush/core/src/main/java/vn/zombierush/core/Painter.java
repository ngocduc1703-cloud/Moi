package vn.zombierush.core;

public interface Painter {
    void rect(float x, float y, float w, float h, float radius, int color);
    void ellipse(float x, float y, float w, float h, int color);
    void line(float x1, float y1, float x2, float y2, float width, int color);
    void polygon(float[] coordinates, int color);
    /** x is the center for center=true, otherwise the left edge. y is baseline. */
    void text(String text, float x, float y, float size, int color, boolean bold, boolean center);
}
