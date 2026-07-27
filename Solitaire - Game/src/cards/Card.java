package cards;

/**
 * Card.java
 * Represents a single playing card. when a card is visible it shows the value and
 * calor
 */
public class Card {

    private final Rank rank;
    private final Suit suit;
    private boolean isVisible;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
        this.isVisible = false;
    }

    public Rank getRank() {
        return rank;
    }

    public int getValue() {
        return rank.getValue();
    }

    public Suit getSuit() {
        return suit;
    }

    public String getColor() {
        return suit.getColor();
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    @Override
    public String toString() {
        if (!isVisible) {
            return "[-]";
        }
        String text = rank.getSymbol() + suit.getSymbol();
        // wrap the card text in its ANSI color code, then reset the
        // color afterward so it doesn't bleed into the rest of the line
        return suit.getAnsiCode() + text + Suit.ANSI_RESET;
    }
}
