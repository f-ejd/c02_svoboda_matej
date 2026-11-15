package model;

// trida pro usecku zadanou dvema koncovymi body
public class Line {
    private Point p1;
    private Point p2;
    private final int color;

    public Line(int x1, int y1, int x2, int y2, int color) {
        this.p1 = new Point(x1, y1);
        this.p2 = new Point(x2, y2);
        this.color = color;
    }

    public Line(Point p1, Point p2, int color) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
    }

    public Point getP1() { return p1; }
    public Point getP2() { return p2; }
    public void setP1(Point p1) { this.p1 = p1; }
    public void setP2(Point p2) { this.p2 = p2; }

    public int getX1() { return p1.getX(); }
    public int getY1() { return p1.getY(); }
    public int getX2() { return p2.getX(); }
    public int getY2() { return p2.getY(); }
    public int getColor() { return color; }

    // Vypocet zarovnani koncoveho bodu na vodorovnou, svislou nebo uhlopricnou (45°) usecku.
    // Vzorec pro zaokrouhleni uhlu pres atan2 dopracovan s pomoci AI.
    public static Point getSnappedPoint(Point start, Point current) {
        int dx = current.getX() - start.getX();
        int dy = current.getY() - start.getY();
        double dist = Math.hypot(dx, dy);
        double angle = Math.atan2(dy, dx);

        // zaokrouhleni na nejblizsi nasobek 45 stupnu (PI / 4)
        double snappedAngle = Math.round(angle / (Math.PI / 4)) * (Math.PI / 4);

        int newX = (int) Math.round(start.getX() + dist * Math.cos(snappedAngle));
        int newY = (int) Math.round(start.getY() + dist * Math.sin(snappedAngle));

        return new Point(newX, newY);
    }
}
