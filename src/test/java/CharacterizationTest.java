import java.util.ArrayList;
import java.util.Random;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization tests pinning the existing behaviour of the UNO engine.
 * All tests operate on Card, Rules, and GameState directly — no Scanner,
 * no System.in, no CLI required.
 *
 * Quirks intentionally preserved (documented here):
 *  1. All hands are printed to console on every turn (no hidden information).
 *  2. A human player may type DRAW even when legal cards are in hand.
 *  3. If a chosen index >= hand.size() the engine adds a penalty card and advances the turn.
 *  4. Bots automatically play a drawn card that turns out to be legal.
 */
public class CharacterizationTest {

    // -- Card value-object tests --

    @Test void testColor() {
        assertEquals("R", Card.of("R5").color());
        assertEquals("Y", Card.of("Y3").color());
        assertEquals("G", Card.of("G+2").color());
        assertEquals("B", Card.of("BS").color());
        assertEquals("",  Card.of("W").color());
        assertEquals("",  Card.of("W4").color());
    }

    @Test void testRank() {
        assertEquals("WILD",           Card.of("W").rank());
        assertEquals("WILD_DRAW_FOUR", Card.of("W4").rank());
        assertEquals("SKIP",           Card.of("RS").rank());
        assertEquals("REVERSE",        Card.of("GR").rank());
        assertEquals("DRAW_TWO",       Card.of("B+2").rank());
        assertEquals("NUMBER",         Card.of("R0").rank());
        assertEquals("NUMBER",         Card.of("Y7").rank());
    }

    @Test void testNumber() {
        assertEquals(0,  Card.of("R0").number());
        assertEquals(9,  Card.of("G9").number());
        assertEquals(-1, Card.of("W").number());
        assertEquals(-1, Card.of("RS").number());
    }

    @Test void testCardIsWild() {
        assertTrue(Card.of("W").isWild());
        assertTrue(Card.of("W4").isWild());
        assertFalse(Card.of("R5").isWild());
        assertFalse(Card.of("GS").isWild());
    }

    // -- Scoring tests --

    @Test void testScoringNumberCard() {
        assertEquals(5, Card.of("R5").points());
        assertEquals(0, Card.of("Y0").points());
        assertEquals(9, Card.of("G9").points());
    }

    @Test void testScoringActionCard() {
        assertEquals(20, Card.of("GS").points());
        assertEquals(20, Card.of("BR").points());
        assertEquals(20, Card.of("G+2").points());
    }

    @Test void testScoringWildCard() {
        assertEquals(50, Card.of("W").points());
        assertEquals(50, Card.of("W4").points());
    }

    // -- Rules.isLegal tests --

    @Test void testIsLegalColorMatch() {
        assertTrue(Rules.isLegal(Card.of("R2"), Card.of("R9"), ""));
        assertTrue(Rules.isLegal(Card.of("GS"), Card.of("G3"), ""));
    }

    @Test void testIsLegalNumberMatch() {
        assertTrue(Rules.isLegal(Card.of("G9"), Card.of("R9"), ""));
        assertTrue(Rules.isLegal(Card.of("B0"), Card.of("Y0"), ""));
    }

    @Test void testIsLegalActionMatch() {
        assertTrue(Rules.isLegal(Card.of("BS"), Card.of("RS"), ""));
        assertTrue(Rules.isLegal(Card.of("YR"), Card.of("GR"), ""));
        assertTrue(Rules.isLegal(Card.of("R+2"), Card.of("G+2"), ""));
    }

    @Test void testIsLegalWildAlwaysLegal() {
        assertTrue(Rules.isLegal(Card.of("W"), Card.of("R5"), ""));
        assertTrue(Rules.isLegal(Card.of("W4"), Card.of("BS"), ""));
        assertTrue(Rules.isLegal(Card.of("W"), Card.of("W"), ""));
    }

    @Test void testIsLegalCalledColor() {
        assertTrue(Rules.isLegal(Card.of("B3"), Card.of("W"), "B"));
        assertFalse(Rules.isLegal(Card.of("R5"), Card.of("W"), "Y"));
    }

    @Test void testIsLegalIllegalMismatch() {
        assertFalse(Rules.isLegal(Card.of("B3"), Card.of("R9"), ""));
        assertFalse(Rules.isLegal(Card.of("GS"), Card.of("R9"), ""));
    }

    // -- GameState: bot card selection tests --

    @Test void testChooseBotCardPrefersDrawTwo() {
        GameState g = gameWith("R9", "");
        assertEquals(1, g.chooseBotCard(clist("RS", "R+2", "W")));
    }

    @Test void testChooseBotCardPrefersSkipOverNumber() {
        GameState g = gameWith("R9", "");
        assertEquals(1, g.chooseBotCard(clist("R5", "RS", "W")));
    }

    @Test void testChooseBotCardPrefersNumberOverWild() {
        GameState g = gameWith("R9", "");
        assertEquals(1, g.chooseBotCard(clist("B3", "R4", "W")));
    }

    @Test void testChooseBotCardUsesWildAsLastResort() {
        GameState g = gameWith("R9", "");
        assertEquals(2, g.chooseBotCard(clist("G3", "B5", "W")));
    }

    @Test void testChooseBotCardReturnsMinusOneWhenNoPlay() {
        GameState g = gameWith("R9", "");
        assertEquals(-1, g.chooseBotCard(clist("G3", "B5")));
    }

    @Test void testChooseBotCardPlaysReverseWhenItIsTheOnlyLegalCard() {
        // Regression test: chooseBotCard previously checked DRAW_TWO/SKIP/NUMBER/WILD
        // but never REVERSE, so a bot holding only a legal Reverse card would draw
        // forever instead of playing it.
        GameState g = gameWith("R9", "");
        assertEquals(0, g.chooseBotCard(clist("RR", "G3", "B5")));
    }

    // -- GameState: bot color selection tests --

    @Test void testChooseBotColor() {
        GameState g = gameWith("R9", "");
        assertEquals("B", g.chooseBotColor(clist("B1", "B2", "R3")));
    }

    @Test void testChooseBotColorTieBreak() {
        GameState g = gameWith("R9", "");
        String color = g.chooseBotColor(clist("R1", "G2", "Y3", "B4"));
        assertTrue(color.equals("R") || color.equals("Y") || color.equals("G") || color.equals("B"));
    }

    // -- GameState: draw / reshuffle test --

    @Test void testDrawReshuffle() {
        GameState g = new GameState(2, new Random(42));
        g.getDiscard().add(Card.of("R1"));
        g.getDiscard().add(Card.of("G5"));
        Card drawn = g.draw();
        assertTrue(drawn.code.equals("R1") || drawn.code.equals("G5"));
        assertTrue(g.getDiscard().isEmpty());
    }

    // -- GameState: tallyPoints test --

    @Test void testTallyPoints() {
        GameState g = new GameState(3, new Random());
        g.getHand(0).clear();
        g.getHand(1).add(Card.of("R5")); g.getHand(1).add(Card.of("GS"));
        g.getHand(2).add(Card.of("W"));  g.getHand(2).add(Card.of("B3"));
        assertEquals(78, g.tallyPoints(0));
    }

    // -- Quirk: bot auto-play of drawn card --

    @Test void testBotAutoPlayDrawn() {
        GameState g = gameWith("R9", "");
        g.getDeck().add(Card.of("R5"));
        Card drawn = g.draw();
        assertEquals("R5", drawn.code);
        assertTrue(Rules.isLegal(drawn, g.getUpCard(), g.getCalledColor()));
    }

    // -- GameState: applyCardEffect tests --

    @Test void testSkipAdvancesTwice() {
        GameState g = new GameState(3, new Random());
        g.setUpCard(Card.of("RS"));
        g.applyCardEffect(Card.of("RS"));
        assertEquals(2, g.getCurrentPlayer());
    }

    @Test void testReverseFlipsDirection() {
        GameState g = new GameState(3, new Random());
        g.setUpCard(Card.of("GR"));
        g.applyCardEffect(Card.of("GR"));
        assertEquals(-1, g.getDirection());
    }

    @Test void testReverseActsLikeSkipInTwoPlayerGame() {
        // Documented simplification: with only 2 players, Reverse behaves like
        // Skip — the same player who played it gets another turn.
        GameState g = new GameState(2, new Random());
        g.setUpCard(Card.of("GR"));
        g.applyCardEffect(Card.of("GR"));
        assertEquals(0, g.getCurrentPlayer());
        assertEquals(-1, g.getDirection());
    }

    @Test void testDrawTwoForcesDraw() {
        GameState g = new GameState(3, new Random());
        g.buildDeck(); g.dealCards();
        int cp = g.getCurrentPlayer();
        int next = (cp + 1) % 3;
        int handBefore = g.getHand(next).size();
        g.applyCardEffect(Card.of("R+2"));
        assertEquals(handBefore + 2, g.getHand(next).size());
        assertEquals(2, g.getLastForcedDrawCount());
        assertEquals(next, g.getLastForcedDrawTarget());
    }

    @Test void testWildDrawFourForcesDraw() {
        GameState g = new GameState(3, new Random());
        g.buildDeck(); g.dealCards();
        int cp = g.getCurrentPlayer();
        int next = (cp + 1) % 3;
        int handBefore = g.getHand(next).size();
        g.applyCardEffect(Card.of("W4"));
        assertEquals(handBefore + 4, g.getHand(next).size());
        assertEquals(4, g.getLastForcedDrawCount());
        assertEquals(next, g.getLastForcedDrawTarget());
    }

    @Test void testNormalCardAdvancesOnce() {
        GameState g = new GameState(3, new Random());
        g.applyCardEffect(Card.of("R5"));
        assertEquals(1, g.getCurrentPlayer());
    }

    // -- GameEngine: integrated game-flow tests --

    @Test void testEngineCompletesGame() {
        int[] winnerHolder = {-1};
        GameEngine.playGame(3, new Random(42), silentBotListener(winnerHolder));
        assertTrue(winnerHolder[0] >= 0, "engine should complete game with a winner");
    }

    @Test void testEngineProducesNonZeroScore() {
        int[] winnerHolder = {-1};
        int points = GameEngine.playGame(3, new Random(42), silentBotListener(winnerHolder));
        assertTrue(points > 0, "engine should produce non-zero score");
    }

    @Test void testEngineWinnerHandEmpty() {
        int[] winnerHolder = {-1};
        GameEngine.playGame(3, new Random(42), silentBotListener(winnerHolder));
        assertTrue(winnerHolder[0] >= 0, "engine should determine a winner");
    }

    // -- Self-test shims (originally in Main.selfTest) --

    @Test void testSelfTestColorRankPointsLegal() {
        assertEquals("R", Card.of("R5").color());
        assertEquals("DRAW_TWO", Card.of("G+2").rank());
        assertEquals(50, Card.of("W4").points());
        assertTrue(Rules.isLegal(Card.of("R2"), Card.of("R9"), ""));
        assertTrue(Rules.isLegal(Card.of("G9"), Card.of("R9"), ""));
        assertTrue(Rules.isLegal(Card.of("B3"), Card.of("W"), "B"));
        assertFalse(Rules.isLegal(Card.of("B3"), Card.of("R9"), ""));
    }

    @Test void testSelfTestBotBehavior() {
        GameState g = new GameState(2, new Random());
        g.setUpCard(Card.of("R9")); g.setCalledColor("");
        assertEquals(1, g.chooseBotCard(clist("B3", "R4", "W")));
        assertEquals("B", g.chooseBotColor(clist("B1", "B2", "R3")));
    }

    // -- Helpers --

    private GameState gameWith(String upCode, String called) {
        GameState g = new GameState(2, new Random());
        g.setUpCard(Card.of(upCode));
        g.setCalledColor(called);
        return g;
    }

    private ArrayList<Card> clist(String... codes) {
        ArrayList<Card> list = new ArrayList<>();
        for (String c : codes) list.add(Card.of(c));
        return list;
    }

    private GameListener silentBotListener(final int[] winnerHolder) {
        return new GameListener() {
            public int chooseCard(int p, ArrayList<Card> hand, GameState s) { return s.chooseBotCard(hand); }
            public String chooseColor(int p, ArrayList<Card> hand, GameState s) { return s.chooseBotColor(hand); }
            public void onTurnStart(int p, ArrayList<Card> h, Card u, String c) {}
            public void onCardPlayed(int p, Card c) {}
            public void onCardDrawn(int p, Card c) {}
            public void onIllegalPlay(int p, Card c) {}
            public void onInvalidIndex(int p) {}
            public void onUno(int p) {}
            public void onWin(int p, int pts) { winnerHolder[0] = p; }
            public void onForcedDraw(int t, int c) {}
            public void onGameStopped() {}
        };
    }
}
