package zones;

import cards.Card;
import java.util.ArrayList;
import java.util.List;


public class Tableau {

    private final List<Card> content;

    public Tableau() {
        content = new ArrayList<>();
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

    public Card lastCard() {
        if (content.isEmpty()) {
            return null;
        }
        return content.get(content.size() - 1);
    }

    public Card bottomVisibleCard() {
        for (Card c : content) {
            if (c.isVisible()) {
                return c;
            }
        }
        return null;
    }

    // can move stack
    public List<Card> visibleStackFrom(Card startCard) {
        List<Card> stack = new ArrayList<>();
        boolean collecting = false;
        for (Card c : content) {
            if (c == startCard) {
                collecting = true;
            }
            if (collecting) {
                stack.add(c);
            }
        }
        return stack;
    }

    public void append(Card card) {
        content.add(card);
    }

    public void appendStack(List<Card> stack) {
        content.addAll(stack);
    }

    public void removeStack(List<Card> stack) {
        content.removeAll(stack);
    }

    public void flipLastCard() {
        Card last = lastCard();
        if (last != null) {
            last.setVisible(true);
        }
    }

    public int size() {
        return content.size();
    }

    public List<Card> getContent() {
        return content;
    }
}
