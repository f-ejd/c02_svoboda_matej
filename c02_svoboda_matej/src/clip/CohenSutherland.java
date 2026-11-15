package clip;

import model.Line;

// Algoritmus Cohen-Sutherland pro orezani usecky obdelnikovym oknem
public class CohenSutherland {
    // 4-bitove kody oblasti
    private static final int INSIDE = 0; // 0000
    private static final int LEFT   = 1; // 0001
    private static final int RIGHT  = 2; // 0010
    private static final int BOTTOM = 4; // 0100
    private static final int TOP    = 8; // 1000

    private static int computeOutCode(double x, double y, int xMin, int yMin, int xMax, int yMax) {
        int code = INSIDE;
        if (x < xMin)      code |= LEFT;
        else if (x > xMax) code |= RIGHT;
        if (y < yMin)      code |= TOP;
        else if (y > yMax) code |= BOTTOM;
        return code;
    }

    // Binarni operace nad outcodes a vypocet pruseciku s hranicemi okna zkontrolovany s pomoci AI.
    public static Line clip(Line line, int xMin, int yMin, int xMax, int yMax, int clippedColor) {
        double x1 = line.getX1();
        double y1 = line.getY1();
        double x2 = line.getX2();
        double y2 = line.getY2();

        int outcode1 = computeOutCode(x1, y1, xMin, yMin, xMax, yMax);
        int outcode2 = computeOutCode(x2, y2, xMin, yMin, xMax, yMax);
        boolean accept = false;

        while (true) {
            if ((outcode1 | outcode2) == 0) {
                // trivialni prijeti - oba body lezi uvnitr okna
                accept = true;
                break;
            } else if ((outcode1 & outcode2) != 0) {
                // trivialni zamitnuti - oba body lezi ve stejnem vnejsim poloprostoru
                break;
            } else {
                // usecka protina hranici okna -> dopocitame prusecik
                double x = 0, y = 0;
                int outcodeOut = (outcode1 != 0) ? outcode1 : outcode2;

                if ((outcodeOut & BOTTOM) != 0) {
                    x = x1 + (x2 - x1) * (yMax - y1) / (y2 - y1);
                    y = yMax;
                } else if ((outcodeOut & TOP) != 0) {
                    x = x1 + (x2 - x1) * (yMin - y1) / (y2 - y1);
                    y = yMin;
                } else if ((outcodeOut & RIGHT) != 0) {
                    y = y1 + (y2 - y1) * (xMax - x1) / (x2 - x1);
                    x = xMax;
                } else if ((outcodeOut & LEFT) != 0) {
                    y = y1 + (y2 - y1) * (xMin - x1) / (x2 - x1);
                    x = xMin;
                }

                if (outcodeOut == outcode1) {
                    x1 = x;
                    y1 = y;
                    outcode1 = computeOutCode(x1, y1, xMin, yMin, xMax, yMax);
                } else {
                    x2 = x;
                    y2 = y;
                    outcode2 = computeOutCode(x2, y2, xMin, yMin, xMax, yMax);
                }
            }
        }

        if (accept) {
            return new Line((int) Math.round(x1), (int) Math.round(y1),
                            (int) Math.round(x2), (int) Math.round(y2), clippedColor);
        }
        return null;
    }
}
