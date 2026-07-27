package engine;

import cards.Card;
import zones.Foundation;
import zones.Tableau;
import zones.Talon;
import zones.Waste;
import java.util.ArrayList;
import java.util.List;

/**
 * GameBoard.java
 * Holds the full board state and performs the initial setup: build the
 * foundations, build the tableau columns, and deal the cards.
 */
public class GameBoard {

    public static final int ACE_ZONE_VALUE = 0;
    public static final int KINGZONE_VALUE = 14;
    public final int tableau_count = 7;
    public final int ace_zone = 4;
    public int change = 0;
    public int area = 1;

    public final Waste waste = new Waste();
    public final Talon talon = new Talon();
    public final List<Tableau> tableaus = new ArrayList<>();
    public final List<Foundation> foundationZones = new ArrayList<>();

    public GameBoard() {
        talon.shuffle();
        createFoundation();
        createTableau();
        dealCards();
    }

    private void createFoundation() {
        area = 1;
        while (area <= ace_zone) {
            foundationZones.add(new Foundation());
            area = area + 1;
        }
    }

    private void createTableau() {
        int area1 = 1;
        while (area1 <= tableau_count) {
            tableaus.add(new Tableau());
            area1 = area1 + 1;
        }
    }

    private void dealCards() {
        int zone = 1;
        while (zone <= tableau_count) {
            int content = 1;
            Tableau tableauPile = tableaus.get(zone - 1);
            while (content <= zone) {
                tableauPile.append(talon.addTopCard());
                content = content + 1;
            }
            tableauPile.flipLastCard();
            zone = zone + 1;
        }
    }

    // ANSI color codes make cards print with invisible escape characters,
    // which throws off simple %-Ns padding. This pads based on the VISIBLE
    // length only, so columns still line up neatly in the terminal.
    private static String padVisible(String text, int width) {
        String visibleOnly = text.replaceAll("\u001B\\[[0-9;]*m", "");
        StringBuilder sb = new StringBuilder(text);
        for (int i = visibleOnly.length(); i < width; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public void displayField() {
        System.out.println("---------------------------------------------------------");

        // foundations, side by side in one row
        for (int i = 0; i < foundationZones.size(); i++) {
            System.out.print(padVisible("Fondation" + (i + 1) + "  ", 10));
        }
        System.out.println();
        for (Foundation zone : foundationZones) {
            Card top = zone.topCard();
            String text = (top == null ? "[ ]" : top.toString()) + "(" + zone.size() + ")      ";
            //String padded = String.format("%-20s");
            System.out.print(padVisible(text, 10));
        }
        System.out.println();
        System.out.println();

        // tableau columns, side by side, cards going down each column
        for (int i = 0; i < tableaus.size(); i++) {
            System.out.print(padVisible("Tab" + (i + 1), 7));
        }
        System.out.println();

        int maxRows = 0;
        for (Tableau t : tableaus) {
            maxRows = Math.max(maxRows, t.size());
        }
        for (int row = 0; row < maxRows; row++) {
            for (Tableau t : tableaus) {
                List<Card> cardList = t.getContent();
                String text = (row < cardList.size()) ? cardList.get(row).toString() : "";
                System.out.print(padVisible(text, 7));
            }
            System.out.println();
        }

        //System.out.println();

        // waste pile: 3 at ta time
        System.out.print("waste (top 3): ");
        for (Card c : waste.getCards()) {
            System.out.print(padVisible(c.toString(), 5));
        }
        System.out.println();

        System.out.println("talon: " + talon.size() + " cards remaining");
        System.out.println("---------------------------------------------------------");
    }
}
