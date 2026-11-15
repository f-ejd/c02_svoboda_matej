package fill;

// Bonus 1: konkretni implementace rozhrani PatternFill (sachovnicovy vzor 6x6 px)
public class CheckerPattern implements PatternFill {
    private final int primaryColor;
    private final int secondaryColor;

    public CheckerPattern(int primaryColor, int secondaryColor) {
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    @Override
    public int paint(int x, int y) {
        int cellX = Math.abs(x) / 6;
        int cellY = Math.abs(y) / 6;
        return ((cellX + cellY) % 2 == 0) ? primaryColor : secondaryColor;
    }
}
