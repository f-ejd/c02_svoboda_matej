package model;

// trida pro bod [x, y] v rastru
public class Point {
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(double x, double y) {
        this.x = (int) Math.round(x);
        this.y = (int) Math.round(y);
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    // vzdalenost ke druhemu bodu (pro chytani vrcholu mysi)
    public double distanceTo(Point other) {
        return Math.hypot(this.x - other.x, this.y - other.y);
    }
}
