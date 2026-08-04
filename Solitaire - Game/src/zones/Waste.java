package zones;

import cards.Card;
import java.util.ArrayList;
import java.util.List;


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


    public void clear() {
        waste.clear();
    }
}
