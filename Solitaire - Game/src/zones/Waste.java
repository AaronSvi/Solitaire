package zones;

import cards.Card;
import java.util.ArrayList;
import java.util.List;

/**
 * Waste.java
 * The face-up waste pile (previously named "Hand" - renamed per project
 * request), built by drawing three cards at a time from the talon.
 *
 * Only the most recently drawn cards (up to 3) are ever shown to the
 * player, matching real Klondike: the rest of the pile is still there
 * underneath, but visually fanned-out only 3 cards deep.
 */
public class Waste {

    private final List<Card> waste;

    public Waste() {
        waste = new ArrayList<>();
    }

    public Card removeLast() {
        if (waste.isEmpty()) {
            return null;
        }
        return waste.remove(waste.size() - 1);
    }

    public Card lastCard() {
        if (waste.isEmpty()) {
            return null;
        }
        return waste.get(waste.size() - 1);
    }

    public void addCard(Card card) {
        card.setVisible(true);
        waste.add(card);
    }

    public boolean isEmpty() {
        return waste.isEmpty();
    }

    public List<Card> getCards() {
        return waste;
    }

    // only the last up-to-3 cards - this is what should be shown on screen
    public List<Card> recentCards() {
        int from = Math.max(0, waste.size() - 3);
        return new ArrayList<>(waste.subList(from, waste.size()));
    }

    public void clear() {
        waste.clear();
    }
}
