package zones;

import cards.Card;
import cards.Rank;
import cards.Suit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Talon {

    private final List<Card> talon;

    public Talon() {
        talon = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                talon.add(new Card(rank, suit));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(talon);
    }

    public Card addTopCard() {
        if (talon.isEmpty()) {
            return null;
        }
        return talon.removeLast();
    }

    public boolean isEmpty() {
        return talon.isEmpty();
    }

    public int size() {
        return talon.size();
    }

    // reuse waste to talon when the talon is 0
    public void loadFrom(Waste waste) {
        talon.clear();
        talon.addAll(waste.getCards());
        for (Card c : talon) {
            c.setVisible(false);
        }
    }
}
