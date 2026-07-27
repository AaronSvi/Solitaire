package zones;

import cards.Card;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * One of the four foundation piles. Each pile is built up in one suit,
 * starting at an Ace (value 1) and ending at a King (value 13).
 */
public class Foundation {

    private final List<Card> content;

    public Foundation() {
        content = new ArrayList<>();
    }

    public Card topCard() {
        if (content.isEmpty()) {
            return null;
        }
        return content.get(content.size() - 1);
    }

    public boolean canAccept(Card card) {
        if (content.isEmpty()) {
            return card.getValue() == 1; // Ace lang
        }
        Card top = topCard();
        return card.getSuit() == top.getSuit() && card.getValue() == top.getValue() + 1;
    }

    public void append(Card card) {
        card.setVisible(true);
        content.add(card);
    }

    public boolean isComplete() {
        return content.size() == 13;
    }

    public int size() {
        return content.size();
    }

    public List<Card> getContent() {
        return content;
    }
}
