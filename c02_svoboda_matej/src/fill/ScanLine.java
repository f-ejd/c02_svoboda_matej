package fill;

import model.Point;
import model.Polygon;
import raster.Raster;
import rasterize.LineRasterizer;
import rasterize.PolygonRasterizer;

import java.util.ArrayList;
import java.util.List;

// Scan-Line algoritmus pro vyplneni polygonu
public class ScanLine implements Filler {
    private final Raster raster;
    private final LineRasterizer lineRasterizer;
    private final Polygon polygon;
    private final int fillColor;
    private final int edgeColor;
    private final PatternFill pattern;

    // pomocna vnitrni trida pro orientovanou hranu polygonu
    private static class Edge {
        int x1, y1, x2, y2;

        Edge(Point p1, Point p2) {
            // serazeni shora dolu (y1 <= y2)
            if (p1.getY() <= p2.getY()) {
                this.x1 = p1.getX();
                this.y1 = p1.getY();
                this.x2 = p2.getX();
                this.y2 = p2.getY();
            } else {
                this.x1 = p2.getX();
                this.y1 = p2.getY();
                this.x2 = p1.getX();
                this.y2 = p1.getY();
            }
        }

        boolean isHorizontal() {
            return y1 == y2;
        }

        // zkraceni hrany o 1 pixel, aby nevznikaly dotahy a pretahy ve vrcholech
        void shorten() {
            y2 -= 1;
        }

        boolean intersects(int y) {
            return y >= y1 && y <= y2;
        }

        // vypocet X souradnice pruseciku hrany s aktualnim radkem Y
        int getIntersectionX(int y) {
            if (y2 == y1) return x1;
            double k = (double) (x2 - x1) / (y2 - y1);
            return (int) Math.round(x1 + k * (y - y1));
        }
    }

    public ScanLine(Raster raster, LineRasterizer lineRasterizer, Polygon polygon, int fillColor, int edgeColor, PatternFill pattern) {
        this.raster = raster;
        this.lineRasterizer = lineRasterizer;
        this.polygon = polygon;
        this.fillColor = fillColor;
        this.edgeColor = edgeColor;
        this.pattern = pattern;
    }

    @Override
    public void fill() {
        int count = polygon.getCount();
        if (count < 3) return;

        List<Edge> edges = new ArrayList<>();
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;

        // priprava hran a nalezeni rozsahu yMin az yMax
        for (int i = 0; i < count; i++) {
            Point p1 = polygon.getPoint(i);
            Point p2 = polygon.getPoint((i + 1) % count);

            if (p1.getY() < yMin) yMin = p1.getY();
            if (p1.getY() > yMax) yMax = p1.getY();

            Edge edge = new Edge(p1, p2);
            if (!edge.isHorizontal()) {
                edge.shorten();
                edges.add(edge);
            }
        }

        // prochazeni vsech radku od yMin do yMax
        for (int y = yMin; y <= yMax; y++) {
            List<Integer> intersections = new ArrayList<>();
            for (Edge edge : edges) {
                if (edge.intersects(y)) {
                    intersections.add(edge.getIntersectionX(y));
                }
            }

            // Vlastni implementace razeni pruseciku (Bubble Sort) misto Collections.sort
            // podle pozadavku zadani
            sortIntersections(intersections);

            // spojeni sudych a lichych pruseciku
            for (int i = 0; i < intersections.size() - 1; i += 2) {
                int xStart = intersections.get(i);
                int xEnd = intersections.get(i + 1);

                if (pattern != null) {
                    for (int x = xStart; x <= xEnd; x++) {
                        raster.setPixel(x, y, pattern.paint(x, y));
                    }
                } else {
                    lineRasterizer.rasterize(new Point(xStart, y), new Point(xEnd, y), fillColor);
                }
            }
        }

        // obtazeni polygonu barvou hranice
        PolygonRasterizer polyRasterizer = new PolygonRasterizer(lineRasterizer);
        polyRasterizer.rasterize(polygon, edgeColor);
    }

    // jednoduche razeni pruseciku od nejmensiho X po nejvetsi (Bubble Sort)
    private void sortIntersections(List<Integer> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j) > list.get(j + 1)) {
                    int temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }
    }
}
