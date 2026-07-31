package cards;

/**
 *
 * The thirteen card ranks. Each constant carries its numeric value
 * (1..13) plus the short symbol used when printing a card (A, 2-10, J, Q, K).
 */
public enum Rank {
    ACE(1, "A"),
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K");

    private final int value;
    private final String symbol;

    Rank(int value, String symbol) {
        this.value = value;
        this.symbol = symbol;
    }

    public int getValue() {
        return value;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Rank fromValue(int value) {
        for (Rank r : values()) {
            if (r.value == value) {
                return r;
            }
        }
        throw new IllegalArgumentException("No rank with value " + value);
    }
}
