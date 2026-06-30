import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Complements CharacterizationTest's testBotAutoPlayDrawn (which covers the
 * legal-drawn-card path) by exercising the other half of draw/pass behaviour:
 * when the drawn card is not legal, the engine must not auto-play it, and the
 * turn passes without discarding it.
 */
public class DrawPassTest {

    @Test void testDrawingAnIllegalCardCausesAPassWithoutPlaying() {
        final GameState g = new GameState(2, new Random());
        g.setUpCard(Card.of("R9"));
        g.setCalledColor("");
        // Hands start with two cards each so the missed-UNO-penalty scan (which
        // runs every turn for every player) has nothing to catch — this test is
        // isolating draw/pass legality, not the UNO-call mechanic.
        g.getHand(0).add(Card.of("B3"));
        g.getHand(0).add(Card.of("G7"));
        g.getHand(1).add(Card.of("Y2"));
        g.getHand(1).add(Card.of("Y8"));
        g.getDeck().add(Card.of("Y4")); // illegal against R9: wrong color, wrong number, not wild

        final int[] turnNumber = {0};
        final boolean[] cardPlayedOnTurnOne = {false};
        final int[] handSizeAfterTurnOneDraw = {-1};

        GameListener listener = new GameListener() {
            public int chooseCard(int p, ArrayList<Card> hand, GameState s) { return -1; }
            public String chooseColor(int p, ArrayList<Card> hand, GameState s) { return "R"; }
            public void onTurnStart(int p, ArrayList<Card> h, Card u, String c) { turnNumber[0]++; }
            public void onCardPlayed(int p, Card c) {
                if (turnNumber[0] == 1) cardPlayedOnTurnOne[0] = true;
            }
            public void onCardDrawn(int p, Card c) {
                if (turnNumber[0] == 1) handSizeAfterTurnOneDraw[0] = g.getHand(0).size();
            }
            public void onIllegalPlay(int p, Card c) {}
            public void onInvalidIndex(int p) {}
            public void onUno(int p) {}
            public void onWin(int p, int pts) {}
            public void onForcedDraw(int t, int c) {}
            public void onGameStopped() {}
        };

        GameEngine.playGame(g, listener);

        assertFalse(cardPlayedOnTurnOne[0], "an illegal drawn card must not be auto-played");
        assertEquals(3, handSizeAfterTurnOneDraw[0], "the illegal card stays in hand instead of being discarded");
    }
}
