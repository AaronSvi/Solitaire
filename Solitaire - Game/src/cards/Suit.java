package cards;


public enum Suit {
    HEARTS("♥", "RED"),
    DIAMONDS("♦", "RED"),
    CLUBS("♣", "BLACK"),
    SPADES("♠", "BLACK");

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

    public String getAnsiCode() {
        return color.equals("RED") ? ANSI_RED : ANSI_GREEN;
    }
}
