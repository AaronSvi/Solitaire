package zones;

import cards.Card;
import java.util.ArrayList;
import java.util.List;


public class Foundation {

    private final List<Card> content;

    public Foundation() {
        content = new ArrayList<>();
    }

    public Card topCard() {
        if (content.isEmpty()) {
            return null;
        }
        return content.getLast();
    }

    public boolean canAccept(Card card) {
        if (content.isEmpty()) {
            return card.getValue() == 1;
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

}
