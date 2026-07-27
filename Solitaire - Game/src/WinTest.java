import cards.Card;
import cards.Rank;
import cards.Suit;
import engine.GameBoard;
import engine.SolitaireGame;
import zones.Foundation;
import java.util.ArrayList;

/**
 * WinTest.java
 * Not part of the game itself - a one-off test proving the Winner path
 * fires when all 4 foundations reach 13 cards.
 *
 *     javac -d . cards/*.java zones/*.java engine/*.java Main.java WinTest.java
 *     java WinTest
 */
public class WinTest {

    public static void main(String[] args) {
        SolitaireGame game = new SolitaireGame();
        GameBoard board = game.getBoard();

        for (var tableau : board.tableaus) {
            tableau.removeStack(new ArrayList<>(tableau.getContent()));
        }
        board.waste.getCards().clear();
        while (!board.talon.isEmpty()) {
            board.talon.addTopCard();
        }

        Suit[] suits = Suit.values();
        for (int s = 0; s < 3; s++) {
            Foundation foundation = board.foundationZones.get(s);
            for (int v = 1; v <= 13; v++) {
                foundation.append(new Card(Rank.fromValue(v), suits[s]));
            }
        }

        Foundation lastFoundation = board.foundationZones.get(3);
        for (int v = 1; v <= 12; v++) {
            lastFoundation.append(new Card(Rank.fromValue(v), suits[3]));
        }

        Card missingKing = new Card(Rank.KING, suits[3]);
        missingKing.setVisible(true);
        board.tableaus.get(0).append(missingKing);

        System.out.println("Starting test: all 4 foundations should reach 13 cards...\n");
        game.run();
    }
}
