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


public class SolitaireGame {

    private static final int ROUND_DELAY_MS = 3000;
    private final GameBoard board;
    private final BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
    private Tableau pendingFlip = null;
    private boolean progressSinceRecycle = false;
    private boolean checkWasteThisRound = false;

    public SolitaireGame() {
        board = new GameBoard();
    }


    public void run() {


        System.out.println("(Each round pauses " + (ROUND_DELAY_MS / 1000) +
                " seconds - press Enter at any time to skip ahead.)");

        while (true) {
            pauseForRound();
            System.out.println("Display Game");
            board.displayField();


            if (pendingFlip != null) {
                Tableau toFlip = pendingFlip;
                pendingFlip = null;
                toFlip.flipLastCard();
                log("Last face down card is turned face-up in " + tableauLabel(toFlip));
                continue;
            }


            if (checkWasteThisRound) {
                checkWasteThisRound = false;
                if (tryWasteMove()) {
                    continue;
                }
            }

            if (foundationComparison()) {
                continue;
            }

            if (tableauComparison()) {
                continue;
            }

            if (board.talon.isEmpty() && progressSinceRecycle) {
                board.talon.loadFrom(board.waste);
                board.waste.clear();
                board.change = 0;
                progressSinceRecycle = false;
                log("Recycle: Waste moved back into Talon");
                continue;
            }

            if (!board.talon.isEmpty() && allComparisonsMade() && board.change == 0) {
                drawFromTalon();
                continue;
            }


            board.change = 0;
            if (!progressPossible()) {
                declareResult();
                break;
            }
        }

        board.displayField();
    }


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
        }
    }

    private void log(String message) {
        System.out.println("LOG: " + message);
        System.out.println("---------------------------------------------------------");

    }


    private String tableauLabel(Tableau t) {
        if (t == null) {
            return "Tableau";
        }
        int idx = board.tableaus.indexOf(t);
        return idx >= 0 ? "Tableau" + (idx + 1) : "Tableau";
    }


    private String foundationLabel(Foundation zone) {
        int idx = board.foundationZones.indexOf(zone);
        return idx >= 0 ? "Foundation" + (idx + 1) : "Foundation";
    }


    private boolean tryWasteMove() {
        Card visibleCard = board.waste.lastCard();
        if (visibleCard == null || !visibleCard.isVisible()) {
            return false;
        }

        for (Foundation zone : board.foundationZones) {
            if (zone.canAccept(visibleCard)) {
                zone.append(visibleCard);
                board.waste.removeLast();
                board.change = board.change + 1;
                progressSinceRecycle = true;
                log(visibleCard + " moved from Waste to " + foundationLabel(zone));
                return true;
            }
        }

        for (Tableau destination : board.tableaus) {
            Card destTop = destination.lastCard();
            boolean valid;
            if (destTop == null) {
                valid = visibleCard.getValue() == 13;
            } else {
                valid = !destTop.getColor().equals(visibleCard.getColor())
                        && visibleCard.getValue() == destTop.getValue() - 1;
            }
            if (valid) {
                destination.append(visibleCard);
                board.waste.removeLast();
                board.change = board.change + 1;
                progressSinceRecycle = true;
                log(visibleCard + " moved from Waste to " + tableauLabel(destination));
                return true;
            }
        }

        return false;
    }


    private boolean foundationComparison() {
        List<Card> candidates = new ArrayList<>();
        for (Tableau t : board.tableaus) {
            if (!t.isEmpty()) {
                candidates.add(t.lastCard());
            }
        }

        for (Card visibleCard : candidates) {
            if (!visibleCard.isVisible()) {
                continue;
            }
            for (Foundation zone : board.foundationZones) {
                if (zone.canAccept(visibleCard)) {
                    Tableau sourceTableau = findTableauContaining(visibleCard);
                    String sourceLabel = tableauLabel(sourceTableau);
                    String destLabel = foundationLabel(zone);

                    zone.append(visibleCard);
                    board.change = board.change + 1;
                    progressSinceRecycle = true;

                    if (sourceTableau != null) {
                        sourceTableau.removeStack(sourceTableau.visibleStackFrom(visibleCard));
                        if (!sourceTableau.isEmpty() && !sourceTableau.lastCard().isVisible()) {
                            // don't flip yet - that happens on its own round next
                            pendingFlip = sourceTableau;
                        }
                    }
                    log("Move: " + visibleCard + " moved from " + sourceLabel +
                            " to " + destLabel);
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

        for (Card visibleCard : candidates) {
            Tableau sourceTableau = findTableauContaining(visibleCard);

            for (Tableau destination : board.tableaus) {
                if (destination == sourceTableau) {
                    continue;
                }
                Card destTop = destination.lastCard();

                boolean valid;
                if (destTop == null) {
                    valid = visibleCard.getValue() == 13;
                    if (valid && sourceTableau != null
                            && sourceTableau.visibleStackFrom(visibleCard).size() == sourceTableau.size()) {
                        valid = false;
                    }
                } else {
                    valid = !destTop.getColor().equals(visibleCard.getColor())
                            && visibleCard.getValue() == destTop.getValue() - 1;
                }

                if (valid) {
                    String sourceLabel = tableauLabel(sourceTableau);
                    String destLabel = tableauLabel(destination);

                    List<Card> stack = sourceTableau.visibleStackFrom(visibleCard);
                    sourceTableau.removeStack(stack);
                    destination.appendStack(stack);
                    log("Move: " + stack + " moved from " + sourceLabel + " to " + destLabel);
                    if (!sourceTableau.isEmpty() && !sourceTableau.lastCard().isVisible()) {
                        // don't flip yet - that happens on its own round next
                        pendingFlip = sourceTableau;
                    }

                    board.change = board.change + 1;
                    progressSinceRecycle = true;
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
        if (!drawnCards.isEmpty()) {
            checkWasteThisRound = true;
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
                || (!board.waste.isEmpty() && progressSinceRecycle);
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
            System.out.println("You Win!");
        } else {
            System.out.println("No more moves — You Lose!");
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


}