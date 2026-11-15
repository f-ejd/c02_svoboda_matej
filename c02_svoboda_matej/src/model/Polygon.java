package model;

import java.util.ArrayList;
import java.util.List;

// trida pro ukladani vrcholu polygonu
public class Polygon {
    private final List<Point> points;

    public Polygon() {
        this.points = new ArrayList<>();
    }

    public Polygon(List<Point> points) {
        this.points = new ArrayList<>(points);
    }

    public void addPoint(Point p) {
        points.add(p);
    }

    public Point getPoint(int index) {
        return points.get(index);
    }

    public void setPoint(int index, Point p) {
        points.set(index, p);
    }

    public void removePoint(int index) {
        if (index >= 0 && index < points.size()) {
            points.remove(index);
        }
    }

    public int getCount() {
        return points.size();
    }

    public List<Point> getPoints() {
        return new ArrayList<>(points);
    }

    public void clear() {
        points.clear();
    }

    // najde index vrcholu v okoli kliknuti mysi (do vzdalenosti maxDist)
    public int findNearestPointIndex(Point click, double maxDist) {
        int bestIndex = -1;
        double minDist = maxDist;
        for (int i = 0; i < points.size(); i++) {
            double d = points.get(i).distanceTo(click);
            if (d < minDist) {
                minDist = d;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    // Bonus: vlozeni noveho vrcholu na nejblizsi hranu polygonu.
    // Vypocet kolme vzdalenosti bodu od usecky (projekce na hranu) napsan s pomoci AI.
    public void insertPointOnNearestEdge(Point click) {
        if (points.size() < 2) {
            points.add(click);
            return;
        }
        int bestInsertIndex = points.size();
        double minDist = Double.MAX_VALUE;

        for (int i = 0; i < points.size(); i++) {
            Point a = points.get(i);
            Point b = points.get((i + 1) % points.size());
            double dist = pointToSegmentDistance(click, a, b);
            if (dist < minDist) {
                minDist = dist;
                bestInsertIndex = i + 1;
            }
        }
        points.add(bestInsertIndex, click);
    }

    private double pointToSegmentDistance(Point p, Point a, Point b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        if (dx == 0 && dy == 0) return p.distanceTo(a);
        double t = ((p.getX() - a.getX()) * dx + (p.getY() - a.getY()) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        double projX = a.getX() + t * dx;
        double projY = a.getY() + t * dy;
        return Math.hypot(p.getX() - projX, p.getY() - projY);
    }
}
