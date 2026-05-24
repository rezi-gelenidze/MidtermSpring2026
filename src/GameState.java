import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * GameState holds all mutable game data and the pure game-logic methods
 * that operate on it.  No I/O happens here — callers (Main) are responsible
 * for all console output and input.
 */
public class GameState {

    ArrayList<Card> deck    = new ArrayList<Card>();
    ArrayList<Card> discard = new ArrayList<Card>();
    ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();
    Card   upCard      = Card.of("R0");
    String calledColor = "";
    int    direction   = 1;
    int    currentPlayer = 0;
    int    playerCount;
    Random random;

    // Set by applyCardEffect so the caller can announce who drew cards.
    int lastForcedDrawCount  = 0;
    int lastForcedDrawTarget = -1;

    GameState(int playerCount, Random random) {
        this.playerCount = playerCount;
        this.random      = random;
        for (int i = 0; i < playerCount; i++) {
            hands.add(new ArrayList<Card>());
        }
    }

    void buildDeck() {
        deck.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (String color : colors) {
            deck.add(Card.of(color + "0"));
            for (int n = 1; n <= 9; n++) {
                deck.add(Card.of(color + n));
                deck.add(Card.of(color + n));
            }
            deck.add(Card.of(color + "S"));
            deck.add(Card.of(color + "S"));
            deck.add(Card.of(color + "R"));
            deck.add(Card.of(color + "R"));
            deck.add(Card.of(color + "+2"));
            deck.add(Card.of(color + "+2"));
        }
        for (int i = 0; i < 4; i++) {
            deck.add(Card.of("W"));
            deck.add(Card.of("W4"));
        }
        Collections.shuffle(deck, random);
    }

    void dealCards() {
        discard.clear();
        for (ArrayList<Card> hand : hands) hand.clear();
        for (int i = 0; i < playerCount; i++) {
            for (int j = 0; j < 7; j++) {
                hands.get(i).add(draw());
            }
        }
        upCard = draw();
        while (upCard.isWild()) {
            discard.add(upCard);
            upCard = draw();
        }
        calledColor   = "";
        direction     = 1;
        currentPlayer = random.nextInt(playerCount);
    }

    Card draw() {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.isEmpty()) return Card.of("W");
        return deck.remove(0);
    }

    void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerCount) currentPlayer = 0;
        if (currentPlayer < 0)           currentPlayer = playerCount - 1;
    }

    /** Sums the point values held in every opponent hand. */
    int tallyPoints(int winner) {
        int points = 0;
        for (int i = 0; i < hands.size(); i++) {
            if (i != winner) {
                for (Card c : hands.get(i)) points += c.points();
            }
        }
        return points;
    }

    /**
     * Applies the card effect for the card just played and advances the turn.
     * Sets lastForcedDrawCount and lastForcedDrawTarget so the caller can
     * announce draws without this method touching I/O.
     */
    void applyCardEffect(Card card) {
        lastForcedDrawCount  = 0;
        lastForcedDrawTarget = -1;
        String r = card.rank();
        if (r.equals("SKIP")) {
            next();
            next();
        } else if (r.equals("REVERSE")) {
            direction *= -1;
            if (playerCount == 2) { next(); next(); } else next();
        } else if (r.equals("DRAW_TWO")) {
            next();
            lastForcedDrawTarget = currentPlayer;
            hands.get(currentPlayer).add(draw());
            hands.get(currentPlayer).add(draw());
            lastForcedDrawCount = 2;
            next();
        } else if (r.equals("WILD_DRAW_FOUR")) {
            next();
            lastForcedDrawTarget = currentPlayer;
            for (int i = 0; i < 4; i++) hands.get(currentPlayer).add(draw());
            lastForcedDrawCount = 4;
            next();
        } else {
            next();
        }
    }

    /** Bot card selection: DRAW_TWO > SKIP > NUMBER > WILD > draw. */
    int chooseBotCard(ArrayList<Card> hand) {
        for (int i = 0; i < hand.size(); i++)
            if (hand.get(i).rank().equals("DRAW_TWO") && Rules.isLegal(hand.get(i), upCard, calledColor)) return i;
        for (int i = 0; i < hand.size(); i++)
            if (hand.get(i).rank().equals("SKIP")     && Rules.isLegal(hand.get(i), upCard, calledColor)) return i;
        for (int i = 0; i < hand.size(); i++)
            if (hand.get(i).rank().equals("NUMBER")   && Rules.isLegal(hand.get(i), upCard, calledColor)) return i;
        for (int i = 0; i < hand.size(); i++)
            if (hand.get(i).isWild()) return i;
        return -1;
    }

    /** Bot color selection: picks the color most represented in the hand. */
    String chooseBotColor(ArrayList<Card> hand) {
        int r = 0, y = 0, g = 0, b = 0;
        for (Card card : hand) {
            String c = card.color();
            if      (c.equals("R")) r++;
            else if (c.equals("Y")) y++;
            else if (c.equals("G")) g++;
            else if (c.equals("B")) b++;
        }
        if (r >= y && r >= g && r >= b) return "R";
        if (y >= r && y >= g && y >= b) return "Y";
        if (g >= r && g >= y && g >= b) return "G";
        return "B";
    }
}
