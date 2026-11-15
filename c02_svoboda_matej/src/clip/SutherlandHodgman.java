package clip;

import model.Point;
import model.Polygon;

import java.util.ArrayList;
import java.util.List;

// Orezani polygonu konvexnim polygonem (Sutherland-Hodgman)
public class SutherlandHodgman {

    // Rezim orientace orezavaciho polygonu (AUTO si ji spocita sam, CW a CCW lze prepnout rucne)
    public enum OrientationMode {
        AUTO("Automatická (Shoelace)"),
        CW("Kladná / Po směru (CW)"),
        CCW("Záporná / Proti směru (CCW)");

        public final String label;
        OrientationMode(String label) { this.label = label; }
    }

    public static Polygon clip(Polygon subjectPolygon, Polygon clipPolygon, OrientationMode mode) {
        if (subjectPolygon.getCount() < 3 || clipPolygon.getCount() < 3) {
            return new Polygon();
        }

        List<Point> outputList = subjectPolygon.getPoints();
        List<Point> clipPoints = clipPolygon.getPoints();

        boolean isClockwise;
        if (mode == OrientationMode.AUTO) {
            isClockwise = isPolygonClockwise(clipPoints);
        } else {
            isClockwise = (mode == OrientationMode.CW);
        }

        int clipCount = clipPoints.size();
        // postupne orezavani kazdou hranou orezavaciho polygonu
        for (int i = 0; i < clipCount; i++) {
            Point edgeStart = clipPoints.get(i);
            Point edgeEnd = clipPoints.get((i + 1) % clipCount);

            List<Point> inputList = new ArrayList<>(outputList);
            outputList.clear();

            if (inputList.isEmpty()) break;

            Point s = inputList.get(inputList.size() - 1);
            for (Point e : inputList) {
                if (isInside(e, edgeStart, edgeEnd, isClockwise)) {
                    if (!isInside(s, edgeStart, edgeEnd, isClockwise)) {
                        outputList.add(computeIntersection(s, e, edgeStart, edgeEnd));
                    }
                    outputList.add(e);
                } else if (isInside(s, edgeStart, edgeEnd, isClockwise)) {
                    outputList.add(computeIntersection(s, e, edgeStart, edgeEnd));
                }
                s = e;
            }
        }

        return new Polygon(outputList);
    }

    // Zjisteni orientace vrcholu orezavaciho polygonu a analyticky vypocet pruseciku dvou primek
    // byly odladene s pomoci AI (osetreni deleni nulou a orientace osy Y smerem dolu).
    public static boolean isPolygonClockwise(List<Point> points) {
        long sum = 0;
        for (int i = 0; i < points.size(); i++) {
            Point p1 = points.get(i);
            Point p2 = points.get((i + 1) % points.size());
            sum += (long) (p2.getX() - p1.getX()) * (p2.getY() + p1.getY());
        }
        return sum < 0;
    }

    // test polohy bodu vuci orientovane hrane pomoci vektoroveho soucinu (cross product)
    private static boolean isInside(Point p, Point edgeStart, Point edgeEnd, boolean isClockwise) {
        long crossProduct = (long) (edgeEnd.getX() - edgeStart.getX()) * (p.getY() - edgeStart.getY())
                          - (long) (edgeEnd.getY() - edgeStart.getY()) * (p.getX() - edgeStart.getX());
        return isClockwise ? (crossProduct >= 0) : (crossProduct <= 0);
    }

    // vypocet pruseciku hrany polygonu s orezavaci primkou
    private static Point computeIntersection(Point s, Point e, Point cp1, Point cp2) {
        double dcX = cp1.getX() - cp2.getX();
        double dcY = cp1.getY() - cp2.getY();
        double dpX = s.getX() - e.getX();
        double dpY = s.getY() - e.getY();

        double n1 = cp1.getX() * cp2.getY() - cp1.getY() * cp2.getX();
        double n2 = s.getX() * e.getY() - s.getY() * e.getX();
        double denominator = dcX * dpY - dcY * dpX;

        if (Math.abs(denominator) < 1e-9) {
            return s;
        }

        double x = (n1 * dpX - n2 * dcX) / denominator;
        double y = (n1 * dpY - n2 * dcY) / denominator;

        return new Point(x, y);
    }
}
