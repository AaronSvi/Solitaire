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
 * SolitaireGame.java
 * The auto-play engine. Each round pauses for 3 seconds so the moves can
 * actually be watched - press Enter at any point during that pause to
 * skip ahead immediately instead of waiting out the full 3 seconds.
 *
 * When a move uncovers a new face-down tableau card, that card is NOT
 * flipped face-up in the same round as the move. The move happens on
 * one round (the newly-uncovered card still shown face-down), and the
 * flip happens as its own, separate round right after.
 */
public class SolitaireGame {

    private static final int ROUND_DELAY_MS = 3000;

    private final GameBoard board;
    private final Set<String> seenSignatures = new HashSet<>(); // stalemate safety net
    private final BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    // a tableau column with a face-down card waiting to be flipped, one
    // round from now - null when nothing is waiting
    private Tableau pendingFlip = null;

    public SolitaireGame() {
        board = new GameBoard();
    }

    public GameBoard getBoard() {
        return board;
    }

    public void run() {
        System.out.println("OUTPUT(\"Display Field\")");
        board.displayField();
        System.out.println("(Each round pauses " + (ROUND_DELAY_MS / 1000) +
                " seconds - press Enter at any time to skip ahead.)");

        while (true) {
            pauseForRound();
            System.out.println("OUTPUT(Display Game)");
            board.displayField();

            // if a card was uncovered last round, flip it now - as its
            // own round, separate from whatever move uncovered it
            if (pendingFlip != null) {
                Tableau toFlip = pendingFlip;
                pendingFlip = null;
                toFlip.flipLastCard();
                log("Flip: " + toFlip.lastCard() + " turned face-up in " + tableauLabel(toFlip));
                System.out.println("OUTPUT(\"Display Game Results\")");
                continue;
            }

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
                log("Recycle: Waste moved back into Talon");
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
    // presses Enter during the wait
    private void pauseForRound() {
        long start = System.currentTimeMillis();
        try {
            while (System.currentTimeMillis() - start < ROUND_DELAY_MS) {
                if (System.in.available() > 0) {
                    consoleReader.readLine(); // consume the Enter press
                    break;
                }
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException ignored) {
            // if input isn't available in this environment, just fall through
        }
    }

    // prints a move/flip/draw log line in one consistent format
    private void log(String message) {
        System.out.println("LOG: " + message);
    }

    // "Tab3", or "Waste" if given null (used when a card's source was the waste pile)
    private String tableauLabel(Tableau t) {
        if (t == null) {
            return "Waste";
        }
        int idx = board.tableaus.indexOf(t);
        return idx >= 0 ? "Tab" + (idx + 1) : "Tableau";
    }

    // "AZ1"
    private String foundationLabel(Foundation zone) {
        int idx = board.foundationZones.indexOf(zone);
        return idx >= 0 ? "AZ" + (idx + 1) : "Foundation";
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
                    Tableau sourceTableau = fromWaste ? null : findTableauContaining(visibleCard);
                    String sourceLabel = fromWaste ? "Waste" : tableauLabel(sourceTableau);
                    String destLabel = foundationLabel(zone);

                    zone.append(visibleCard);
                    board.change = board.change + 1;

                    if (fromWaste) {
                        board.waste.removeLast();
                    } else if (sourceTableau != null) {
                        sourceTableau.removeStack(sourceTableau.visibleStackFrom(visibleCard));
                        if (!sourceTableau.isEmpty() && !sourceTableau.lastCard().isVisible()) {
                            // don't flip yet - that happens on its own round next
                            pendingFlip = sourceTableau;
                        }
                    }
                    log("Move: " + visibleCard + " moved from " + sourceLabel +
                            " to " + destLabel + " (foundation)");
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
                    String sourceLabel = fromWaste ? "Waste" : tableauLabel(sourceTableau);
                    String destLabel = tableauLabel(destination);

                    if (fromWaste) {
                        destination.append(visibleCard);
                        board.waste.removeLast();
                        log("Move: " + visibleCard + " moved from " + sourceLabel + " to " + destLabel);
                    } else {
                        List<Card> stack = sourceTableau.visibleStackFrom(visibleCard);
                        sourceTableau.removeStack(stack);
                        destination.appendStack(stack);
                        log("Move: " + stack + " moved from " + sourceLabel + " to " + destLabel);
                        if (!sourceTableau.isEmpty() && !sourceTableau.lastCard().isVisible()) {
                            // don't flip yet - that happens on its own round next
                            pendingFlip = sourceTableau;
                        }
                    }
                    board.change = board.change + 1;
                    return true;
                }
            }
        }
        return false;
    }

    private void drawFromTalon() {
        List<Card> drawnCards = new ArrayList<>();
        while (drawnCards.size() < 3 && !board.talon.isEmpty()) {
            Card c = board.talon.addTopCard();
            board.waste.addCard(c);
            drawnCards.add(c);
        }
        log("Draw: " + drawnCards + " drawn from Talon to Waste");
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
            System.out.println("OUTPUT(\"Winner\")");
            System.out.println("OUTPUT(\"13 Cards are in each Foundation\")");
            System.out.println("OUTPUT(\"Display Results\")");
        } else {
            System.out.println("OUTPUT(\"Lose\")");
            System.out.println("OUTPUT(\"No More Possible Moves\")");
            System.out.println("OUTPUT(\"Display Results\")");
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
        // pendingFlip matters too - otherwise two states that only differ by
        // "is a flip about to happen" would look identical to the stalemate check
        sb.append("pendingFlip:").append(pendingFlip == null ? "no" : tableauLabel(pendingFlip));
        return sb.toString();
    }
}