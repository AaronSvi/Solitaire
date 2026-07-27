package cards;

/**
 * The four card suits, each tagged with a color ("RED" or "BLACK") plus
 * the matching ANSI escape code used to print that color in a terminal.
 * Per project request: black suits print in GREEN in the terminal
 * (instead of black, which is invisible on a dark terminal background).
 * Green will act as black
 */
public enum Suit {
    HEARTS("♥", "RED"),
    DIAMONDS("♦", "RED"),
    CLUBS("♣", "BLACK"),
    SPADES("♠", "BLACK");

    //IntelliJ's built-in run console. color.
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    private final String symbol;
    private final String color;

    Suit(String symbol, String color) {
        this.symbol = symbol;
        this.color = color;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getColor() {
        return color;
    }

    // the actual ANSI code to print before the card's text
    public String getAnsiCode() {
        return color.equals("RED") ? ANSI_RED : ANSI_GREEN;
    }
}
