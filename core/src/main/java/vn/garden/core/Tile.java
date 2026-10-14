package vn.garden.core;

/** Logical coordinates are independent of screen resolution. */
public final class Tile {
    public final int id, layer;
    public final float x, y;
    public int kind;
    public boolean removed;
    public Tile(int id, int kind, float x, float y, int layer) {
        this.id = id; this.kind = kind; this.x = x; this.y = y; this.layer = layer;
    }
}
