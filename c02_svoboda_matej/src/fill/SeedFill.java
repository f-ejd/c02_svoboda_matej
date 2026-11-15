package fill;

import model.Point;
import raster.Raster;

import java.util.ArrayDeque;
import java.util.Deque;

// Seminkove vyplnovani oblasti (zapouzdreno do tridy)
// Bonus: reseno nerekurzivne pomoci zasobniku (ArrayDeque), aby pri vetsi plose nespadl StackOverflowError
public class SeedFill implements Filler {
    private final Raster raster;
    private final int startX, startY;
    private final int fillColor;
    private final int targetColor; // bud barva pozadi nebo barva hranice
    private final boolean isBoundaryMode;
    private final PatternFill pattern; // pokud neni null, vyplnuje se vzorem podle rozhrani PatternFill

    public SeedFill(Raster raster, int startX, int startY, int fillColor, int targetColor, boolean isBoundaryMode, PatternFill pattern) {
        this.raster = raster;
        this.startX = startX;
        this.startY = startY;
        this.fillColor = fillColor & 0xFFFFFF;
        this.targetColor = targetColor & 0xFFFFFF;
        this.isBoundaryMode = isBoundaryMode;
        this.pattern = pattern;
    }

    @Override
    public void fill() {
        // kontrola jestli je seminko vubec na platne
        if (startX < 0 || startX >= raster.getWidth() || startY < 0 || startY >= raster.getHeight()) {
            return;
        }

        int initialColor = raster.getPixel(startX, startY);
        if (!isBoundaryMode && initialColor == fillColor && pattern == null) return;
        if (isBoundaryMode && initialColor == targetColor) return;

        // 2D pole navstivenych pixelu - nutne hlavne pri vyplnovani vzorem,
        // aby se algoritmus nezacyklil (konzultovano s AI)
        boolean[][] visited = new boolean[raster.getWidth()][raster.getHeight()];

        Deque<Point> stack = new ArrayDeque<>();
        stack.push(new Point(startX, startY));

        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int x = p.getX();
            int y = p.getY();

            if (x < 0 || x >= raster.getWidth() || y < 0 || y >= raster.getHeight()) {
                continue;
            }
            if (visited[x][y]) {
                continue;
            }

            int currentColor = raster.getPixel(x, y);

            if (shouldFill(currentColor)) {
                visited[x][y] = true;
                int finalColor = (pattern != null) ? pattern.paint(x, y) : fillColor;
                raster.setPixel(x, y, finalColor);

                // pridani 4-sousedu do zasobniku
                stack.push(new Point(x + 1, y));
                stack.push(new Point(x - 1, y));
                stack.push(new Point(x, y + 1));
                stack.push(new Point(x, y - 1));
            }
        }
    }

    // rozhodnuti jestli pixel obarvit podle zvoleneho rezimu (pozadi vs hranice)
    private boolean shouldFill(int currentColor) {
        if (isBoundaryMode) {
            return currentColor != targetColor && (pattern != null || currentColor != fillColor); // zastavi se o barvu hranice
        } else {
            return currentColor == targetColor; // vyplnuje pouze pixely s puvodni barvou pozadi
        }
    }
}
