package rasterize;

import model.Point;
import model.Polygon;

// trida pro vykresleni obvodu polygonu
public class PolygonRasterizer {
    private final LineRasterizer lineRasterizer;

    public PolygonRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void rasterize(Polygon polygon, int color) {
        int count = polygon.getCount();
        if (count < 2) return;

        // pospojovani vsech vrcholu vcetne posledniho s prvnim
        for (int i = 0; i < count; i++) {
            Point p1 = polygon.getPoint(i);
            Point p2 = polygon.getPoint((i + 1) % count);
            lineRasterizer.rasterize(p1, p2, color);
        }
    }
}
