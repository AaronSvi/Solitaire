package engine;

import cards.Card;
import zones.Foundation;
import zones.Tableau;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**

 * press enter for next round
 */
public class SolitaireGame {

    private static final int ROUND_DELAY_MS = 3000;

    private final GameBoard board;
    private final Set<String> seenSignatures = new HashSet<>(); // stalemate safety net
    private final BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    public SolitaireGame() {
        board = new GameBoard();
    }

    public GameBoard getBoard() {
        return board;
    }

    public void run() {
        System.out.println("Display Field");
        board.displayField();
        System.out.println("Press Enter for the next round");
        System.out.println("(Each round pauses " + (ROUND_DELAY_MS / 1000) + " seconds - press Enter at any time to skip ahead.)");

        while (true) {
            pauseForRound();
            System.out.println("Display Game");
            board.displayField();

            if (foundationComparison()) {
                continue;
            }

            if (tableauComparison()) {
                continue;
            }

            if (board.talon.isEmpty() && board.change > 0) {
                board.talon.loadFrom(board.waste);
                board.waste.clear();
                board.change = 0;
                continue;
            }

            if (!board.talon.isEmpty() && allComparisonsMade() && board.change == 0) {
                drawFromTalon();
                continue;
            }

            // END GAME CHECK
            board.change = 0;
            if (!progressPossible()) {
                declareResult();
                break;
            }
            String signature = computeBoardSignature();
            if (!seenSignatures.add(signature)) {
                declareResult();
                break;
            }
            continue;
        }

        board.displayField();
    }

    // pauses up to ROUND_DELAY_MS, but returns immediately if the user
    // presses Enter for the next round
    private void pauseForRound() {
        long start = System.currentTimeMillis();
        try {
            while (System.currentTimeMillis() - start < ROUND_DELAY_MS) {
                if (System.in.available() > 0) {
                    consoleReader.readLine(); // Enter press
                    break;
                }
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException ignored) {
            // if input isn't available in this environment, just fall through
        }
    }

    private boolean foundationComparison() {
        List<Card> candidates = new ArrayList<>();
        for (Tableau t : board.tableaus) {
            if (!t.isEmpty()) {
                candidates.add(t.lastCard());
            }
        }
        if (board.waste.lastCard() != null) {
            candidates.add(board.waste.lastCard());
        }

        for (Card visibleCard : candidates) {
            if (!visibleCard.isVisible()) {
                continue;
            }
            for (Foundation zone : board.foundationZones) {
                if (zone.canAccept(visibleCard)) {
                    boolean fromWaste = (visibleCard == board.waste.lastCard());
                    zone.append(visibleCard);
                    board.change = board.change + 1;

                    if (fromWaste) {
                        board.waste.removeLast();
                    } else {
                        Tableau sourceTableau = findTableauContaining(visibleCard);
                        if (sourceTableau != null) {
                            sourceTableau.removeStack(sourceTableau.visibleStackFrom(visibleCard));
                            if (!sourceTableau.isEmpty() && !sourceTableau.lastCard().isVisible()) {
                                sourceTableau.flipLastCard();
                            }
                        }
                    }
                    System.out.println(" Move card to Fo" +
                            "undation: " + visibleCard + " -> foundation");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tableauComparison() {
        List<Card> candidates = new ArrayList<>();
        for (Tableau t : board.tableaus) {
            Card bottomVisible = t.bottomVisibleCard();
            if (bottomVisible != null) {
                candidates.add(bottomVisible);
            }
        }
        if (board.waste.lastCard() != null) {
            candidates.add(board.waste.lastCard());
        }

        for (Card visibleCard : candidates) {
            Tableau sourceTableau = findTableauContaining(visibleCard);
            boolean fromWaste = (visibleCard == board.waste.lastCard());

            for (Tableau destination : board.tableaus) {
                if (destination == sourceTableau) {
                    continue;
                }
                Card destTop = destination.lastCard();

                boolean valid;
                if (destTop == null) {
                    valid = visibleCard.getValue() == 13;
                    if (valid && !fromWaste && sourceTableau != null
                            && sourceTableau.visibleStackFrom(visibleCard).size() == sourceTableau.size()) {
                        valid = false;
                    }
                } else {
                    valid = !destTop.getColor().equals(visibleCard.getColor())
                            && visibleCard.getValue() == destTop.getValue() - 1;
                }

                if (valid) {
                    if (fromWaste) {
                        destination.append(visibleCard);
                        board.waste.removeLast();
                    } else {
                        List<Card> stack = sourceTableau.visibleStackFrom(visibleCard);
                        sourceTableau.removeStack(stack);
                        destination.appendStack(stack);
                        if (!sourceTableau.isEmpty() && !sourceTableau.lastCard().isVisible()) {
                            sourceTableau.flipLastCard();
                            System.out.println("Display Game Results");
                        }
                    }
                    board.change = board.change + 1;
                    System.out.println("Tableau move: " + visibleCard + " -> tableau");
                    return true;
                }
            }
        }
        return false;
    }

    private void drawFromTalon() {
        int drawn = 0;
        while (drawn < 3 && !board.talon.isEmpty()) {
            board.waste.addCard(board.talon.addTopCard());
            drawn = drawn + 1;
        }
        System.out.println("Change Made");
        System.out.println("Display Field");
        //board.displayField();
    }

    private boolean allComparisonsMade() {
        return !hasFoundationMove() && !hasTableauMove();
    }

    private boolean hasFoundationMove() {
        List<Card> candidates = new ArrayList<>();
        for (Tableau t : board.tableaus) {
            if (!t.isEmpty() && t.lastCard().isVisible()) {
                candidates.add(t.lastCard());
            }
        }
        if (board.waste.lastCard() != null) {
            candidates.add(board.waste.lastCard());
        }
        for (Card c : candidates) {
            for (Foundation zone : board.foundationZones) {
                if (zone.canAccept(c)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasTableauMove() {
        List<Card> candidates = new ArrayList<>();
        for (Tableau t : board.tableaus) {
            Card bottomVisible = t.bottomVisibleCard();
            if (bottomVisible != null) {
                candidates.add(bottomVisible);
            }
        }
        if (board.waste.lastCard() != null) {
            candidates.add(board.waste.lastCard());
        }
        for (Card c : candidates) {
            Tableau source = findTableauContaining(c);
            for (Tableau destination : board.tableaus) {
                if (destination == source) {
                    continue;
                }
                Card destTop = destination.lastCard();
                if (destTop == null) {
                    boolean wholeColumnIsThisCard = source != null
                            && source.visibleStackFrom(c).size() == source.size();
                    if (c.getValue() == 13 && !wholeColumnIsThisCard) {
                        return true;
                    }
                } else if (!destTop.getColor().equals(c.getColor())
                        && c.getValue() == destTop.getValue() - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean progressPossible() {
        return hasFoundationMove() || hasTableauMove() || !board.talon.isEmpty()
                || (!board.waste.isEmpty() && board.change > 0);
    }

    private boolean allFoundationsComplete() {
        for (Foundation zone : board.foundationZones) {
            if (!zone.isComplete()) {
                return false;
            }
        }
        return true;
    }

    private void declareResult() {
        if (allFoundationsComplete()) {
            System.out.println("Winner");
            System.out.println("13 Cards are in each Foundation");
            System.out.println("Display Results");
        } else {
            System.out.println("Lose");
            System.out.println("No More Possible Moves");
            System.out.println("Display Results");
        }
    }

    private Tableau findTableauContaining(Card card) {
        for (Tableau t : board.tableaus) {
            if (t.getContent().contains(card)) {
                return t;
            }
        }
        return null;
    }

    private String computeBoardSignature() {
        StringBuilder sb = new StringBuilder();
        for (Tableau t : board.tableaus) {
            for (Card c : t.getContent()) {
                sb.append(c.getValue()).append(c.getSuit()).append(c.isVisible() ? "1" : "0");
            }
            sb.append("|");
        }
        for (Foundation zone : board.foundationZones) {
            sb.append(zone.size()).append(",");
        }
        sb.append("talon:").append(board.talon.size());
        sb.append("waste:").append(board.waste.getCards().size());
        return sb.toString();
    }
}
