package engine;

import cards.Card;
import zones.Foundation;
import zones.Tableau;
import zones.Talon;
import zones.Waste;
import java.util.ArrayList;
import java.util.List;


public class GameBoard {


    public final int tableau_count = 7;
    public final int foundation_zone = 4;
    public int change = 0;
    public int area = 1;

    public final Waste waste = new Waste();
    public final Talon talon = new Talon();
    public final List<Tableau> tableau = new ArrayList<>();
    public final List<Foundation> foundationZones = new ArrayList<>();

    public GameBoard() {
        talon.shuffle();
        createFoundation();
        createTableau();
        dealCards();
    }

    private void createFoundation() {

        while (area <= foundation_zone) {
            foundationZones.add(new Foundation());
            area = area + 1;
        }
    }

    private void createTableau() {
         area = 1;
        while (area <= tableau_count) {
            tableau.add(new Tableau());
            area = area + 1;
        }
    }

    private void dealCards() {
        int zone = 1;
        while (zone <= tableau_count) {
            int content = 1;
            Tableau tableauPile = tableau.get(zone - 1);
            while (content <= zone) {
                tableauPile.append(talon.addTopCard());
                content = content + 1;
            }
            tableauPile.flipLastCard();
            zone = zone + 1;
        }
    }

    private static String padVisible(String text, int width) {
        String visibleOnly = text.replaceAll("\u001B\\[[0-9;]*m", "");
        return text + " ".repeat(Math.max(0, width - visibleOnly.length()));
    }

    public void displayField() {


        System.out.print("waste: ");
        for (Card c : waste.getCards()) {
            System.out.print(padVisible(c.toString(), 5));
        }
        System.out.println();

        System.out.println("talon: " + talon.size() + " cards remaining");
        System.out.println();


        for (int i = 0; i < foundationZones.size(); i++) {
            System.out.print(padVisible("Foundation" + (i + 1) + "  ", 10));
        }
        System.out.println();
        for (Foundation zone : foundationZones) {
            Card top = zone.topCard();
            String text = (top == null ? "[ ]" : top.toString()) + "(" + zone.size() + ")      ";
            System.out.print(padVisible(text, 12));
        }
        System.out.println();
        System.out.println();

        for (int i = 0; i < tableau.size(); i++) {
            System.out.print(padVisible("Tableau" + (i + 1), 10));
        }
        System.out.println();

        int maxRows = 0;
        for (Tableau t : tableau) {
            maxRows = Math.max(maxRows, t.size());
        }
        for (int row = 0; row < maxRows; row++) {
            for (Tableau t : tableau) {
                List<Card> cardList = t.getContent();
                String text = (row < cardList.size()) ? cardList.get(row).toString() : "";
                System.out.print(padVisible(text, 10));
            }
            System.out.println();
        }

        System.out.println("---------------------------------------------------------");
    }
}
