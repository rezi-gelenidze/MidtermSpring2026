import java.util.ArrayList;

/**
 * Characterization tests pinning the existing behaviour of the UNO engine.
 * All tests operate on Card, Rules, and GameState directly — no Scanner,
 * no System.in, no CLI required.  This demonstrates that the game logic
 * is fully testable without running the full CLI game.
 *
 * Quirks intentionally preserved (documented here):
 *  1. All hands are printed to console on every turn (no hidden information).
 *  2. A human player may type DRAW even when legal cards are in hand; the
 *     engine never forces a play on the human's behalf.
 *  3. If a chosen index >= hand.size() the engine adds a penalty card and
 *     advances the turn (safety guard — not normally reachable via normal input).
 *  4. Bots automatically play a drawn card that turns out to be legal.
 */
public class CharacterizationTest {
    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        testColor();
        testRank();
        testNumber();
        testCardIsWild();
        testScoringNumberCard();
        testScoringActionCard();
        testScoringWildCard();
        testIsLegalColorMatch();
        testIsLegalNumberMatch();
        testIsLegalActionMatch();
        testIsLegalWildAlwaysLegal();
        testIsLegalCalledColor();
        testIsLegalIllegalMismatch();
        testChooseBotCardPrefersDrawTwo();
        testChooseBotCardPrefersSkipOverNumber();
        testChooseBotCardPrefersNumberOverWild();
        testChooseBotCardUsesWildAsLastResort();
        testChooseBotCardReturnsMinusOneWhenNoPlay();
        testChooseBotColor();
        testChooseBotColorTieBreak();
        testDrawReshuffle();
        testTallyPoints();
        testBotAutoPlayDrawn();
        testSkipAdvancesTwice();
        testReverseFlipsDirection();
        testDrawTwoForcesDraw();
        testWildDrawFourForcesDraw();
        testNormalCardAdvancesOnce();

        System.out.println("\nCharacterizationTest: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) System.exit(1);
    }

    // -----------------------------------------------------------------------
    // Card value-object tests
    // -----------------------------------------------------------------------

    static void testColor() {
        assertEqual("color R5",  "R", Card.of("R5").color());
        assertEqual("color Y3",  "Y", Card.of("Y3").color());
        assertEqual("color G+2", "G", Card.of("G+2").color());
        assertEqual("color BS",  "B", Card.of("BS").color());
        assertEqual("color W",   "",  Card.of("W").color());
        assertEqual("color W4",  "",  Card.of("W4").color());
    }

    static void testRank() {
        assertEqual("rank W",   "WILD",           Card.of("W").rank());
        assertEqual("rank W4",  "WILD_DRAW_FOUR", Card.of("W4").rank());
        assertEqual("rank RS",  "SKIP",           Card.of("RS").rank());
        assertEqual("rank GR",  "REVERSE",        Card.of("GR").rank());
        assertEqual("rank B+2", "DRAW_TWO",       Card.of("B+2").rank());
        assertEqual("rank R0",  "NUMBER",         Card.of("R0").rank());
        assertEqual("rank Y7",  "NUMBER",         Card.of("Y7").rank());
    }

    static void testNumber() {
        assertEqual("number R0", 0,  Card.of("R0").number());
        assertEqual("number G9", 9,  Card.of("G9").number());
        assertEqual("number W",  -1, Card.of("W").number());
        assertEqual("number RS", -1, Card.of("RS").number());
    }

    static void testCardIsWild() {
        assertTrue("W isWild",   Card.of("W").isWild());
        assertTrue("W4 isWild",  Card.of("W4").isWild());
        assertFalse("R5 not wild", Card.of("R5").isWild());
        assertFalse("GS not wild", Card.of("GS").isWild());
    }

    // -----------------------------------------------------------------------
    // Scoring tests
    // -----------------------------------------------------------------------

    static void testScoringNumberCard() {
        assertEqual("points R5", 5, Card.of("R5").points());
        assertEqual("points Y0", 0, Card.of("Y0").points());
        assertEqual("points G9", 9, Card.of("G9").points());
    }

    static void testScoringActionCard() {
        assertEqual("points GS",  20, Card.of("GS").points());
        assertEqual("points BR",  20, Card.of("BR").points());
        assertEqual("points G+2", 20, Card.of("G+2").points());
    }

    static void testScoringWildCard() {
        assertEqual("points W",  50, Card.of("W").points());
        assertEqual("points W4", 50, Card.of("W4").points());
    }

    // -----------------------------------------------------------------------
    // Rules.isLegal tests
    // -----------------------------------------------------------------------

    static void testIsLegalColorMatch() {
        assertTrue("color match R2 on R9",
                Rules.isLegal(Card.of("R2"), Card.of("R9"), ""));
        assertTrue("color match GS on G3",
                Rules.isLegal(Card.of("GS"), Card.of("G3"), ""));
    }

    static void testIsLegalNumberMatch() {
        assertTrue("number match G9 on R9",
                Rules.isLegal(Card.of("G9"), Card.of("R9"), ""));
        assertTrue("number match B0 on Y0",
                Rules.isLegal(Card.of("B0"), Card.of("Y0"), ""));
    }

    static void testIsLegalActionMatch() {
        assertTrue("skip match BS on RS",
                Rules.isLegal(Card.of("BS"), Card.of("RS"), ""));
        assertTrue("reverse match YR on GR",
                Rules.isLegal(Card.of("YR"), Card.of("GR"), ""));
        assertTrue("draw-two match R+2 on G+2",
                Rules.isLegal(Card.of("R+2"), Card.of("G+2"), ""));
    }

    static void testIsLegalWildAlwaysLegal() {
        assertTrue("W legal on R5",
                Rules.isLegal(Card.of("W"), Card.of("R5"), ""));
        assertTrue("W4 legal on BS",
                Rules.isLegal(Card.of("W4"), Card.of("BS"), ""));
        assertTrue("W legal on W up-card",
                Rules.isLegal(Card.of("W"), Card.of("W"), ""));
    }

    static void testIsLegalCalledColor() {
        assertTrue("called B: B3 on W",
                Rules.isLegal(Card.of("B3"), Card.of("W"), "B"));
        assertFalse("called Y: R5 on W",
                Rules.isLegal(Card.of("R5"), Card.of("W"), "Y"));
    }

    static void testIsLegalIllegalMismatch() {
        assertFalse("mismatch B3 on R9",
                Rules.isLegal(Card.of("B3"), Card.of("R9"), ""));
        assertFalse("mismatch GS on R9",
                Rules.isLegal(Card.of("GS"), Card.of("R9"), ""));
    }

    // -----------------------------------------------------------------------
    // GameState: bot card selection tests
    // -----------------------------------------------------------------------

    static void testChooseBotCardPrefersDrawTwo() {
        GameState g = gameWith("R9", "");
        assertEqual("bot prefers DRAW_TWO", 1, g.chooseBotCard(clist("RS", "R+2", "W")));
    }

    static void testChooseBotCardPrefersSkipOverNumber() {
        GameState g = gameWith("R9", "");
        assertEqual("bot prefers SKIP over NUMBER", 1, g.chooseBotCard(clist("R5", "RS", "W")));
    }

    static void testChooseBotCardPrefersNumberOverWild() {
        GameState g = gameWith("R9", "");
        assertEqual("bot prefers NUMBER over WILD", 1, g.chooseBotCard(clist("B3", "R4", "W")));
    }

    static void testChooseBotCardUsesWildAsLastResort() {
        GameState g = gameWith("R9", "");
        assertEqual("bot uses WILD as last resort", 2, g.chooseBotCard(clist("G3", "B5", "W")));
    }

    static void testChooseBotCardReturnsMinusOneWhenNoPlay() {
        GameState g = gameWith("R9", "");
        assertEqual("bot returns -1 when no play", -1, g.chooseBotCard(clist("G3", "B5")));
    }

    // -----------------------------------------------------------------------
    // GameState: bot color selection tests
    // -----------------------------------------------------------------------

    static void testChooseBotColor() {
        GameState g = gameWith("R9", "");
        assertEqual("chooseBotColor picks most common", "B", g.chooseBotColor(clist("B1", "B2", "R3")));
    }

    static void testChooseBotColorTieBreak() {
        GameState g = gameWith("R9", "");
        String color = g.chooseBotColor(clist("R1", "G2", "Y3", "B4"));
        assertTrue("chooseBotColor returns valid color",
                color.equals("R") || color.equals("Y") || color.equals("G") || color.equals("B"));
    }

    // -----------------------------------------------------------------------
    // GameState: draw / reshuffle test
    // -----------------------------------------------------------------------

    static void testDrawReshuffle() {
        GameState g = new GameState(2, new java.util.Random(42));
        g.getDiscard().add(Card.of("R1"));
        g.getDiscard().add(Card.of("G5"));
        Card drawn = g.draw();
        assertTrue("draw reshuffles discard when deck empty",
                drawn.code.equals("R1") || drawn.code.equals("G5"));
        assertTrue("discard cleared after reshuffle", g.getDiscard().isEmpty());
    }

    // -----------------------------------------------------------------------
    // GameState: tallyPoints test
    // -----------------------------------------------------------------------

    static void testTallyPoints() {
        // 3-player game: winner=0, p1 holds R5+GS=25, p2 holds W+B3=53 => 78
        GameState g = new GameState(3, new java.util.Random());
        g.getHand(0).clear();
        g.getHand(1).add(Card.of("R5")); g.getHand(1).add(Card.of("GS"));
        g.getHand(2).add(Card.of("W"));  g.getHand(2).add(Card.of("B3"));
        assertEqual("tallyPoints sums opponents", 78, g.tallyPoints(0));
    }

    // -----------------------------------------------------------------------
    // Quirk: bot auto-play of drawn card (components test)
    // -----------------------------------------------------------------------

    static void testBotAutoPlayDrawn() {
        // When a bot draws a card that is legal, the engine sets chosen = hand.size()-1.
        // This test pins the component: draw() returns the expected card and isLegal() is true.
        GameState g = gameWith("R9", "");
        g.getDeck().add(Card.of("R5"));
        Card drawn = g.draw();
        assertEqual("drawn card code", "R5", drawn.code);
        assertTrue("drawn card is legal — bot would auto-play it",
                Rules.isLegal(drawn, g.getUpCard(), g.getCalledColor()));
    }

    // -----------------------------------------------------------------------
    // GameState: applyCardEffect tests
    // -----------------------------------------------------------------------

    static void testSkipAdvancesTwice() {
        GameState g = new GameState(3, new java.util.Random());
        g.setUpCard(Card.of("RS"));
        g.applyCardEffect(Card.of("RS"));
        assertEqual("SKIP advances turn twice from player 0", 2, g.getCurrentPlayer());
    }

    static void testReverseFlipsDirection() {
        GameState g = new GameState(3, new java.util.Random());
        g.setUpCard(Card.of("GR"));
        g.applyCardEffect(Card.of("GR"));
        assertEqual("REVERSE changes direction", -1, g.getDirection());
    }

    static void testDrawTwoForcesDraw() {
        GameState g = new GameState(3, new java.util.Random());
        g.buildDeck();
        g.dealCards();
        int p1Start = g.getHand(1).size();
        g.applyCardEffect(Card.of("R+2"));
        assertEqual("DRAW_TWO: forced player draws 2", p1Start + 2, g.getHand(1).size());
        assertEqual("DRAW_TWO: sets lastForcedDrawCount", 2, g.lastForcedDrawCount);
        assertEqual("DRAW_TWO: sets lastForcedDrawTarget", 1, g.lastForcedDrawTarget);
    }

    static void testWildDrawFourForcesDraw() {
        GameState g = new GameState(3, new java.util.Random());
        g.buildDeck();
        g.dealCards();
        int p1Start = g.getHand(1).size();
        g.applyCardEffect(Card.of("W4"));
        assertEqual("WILD_DRAW_FOUR: forced player draws 4", p1Start + 4, g.getHand(1).size());
        assertEqual("WILD_DRAW_FOUR: sets lastForcedDrawCount", 4, g.lastForcedDrawCount);
        assertEqual("WILD_DRAW_FOUR: sets lastForcedDrawTarget", 1, g.lastForcedDrawTarget);
    }

    static void testNormalCardAdvancesOnce() {
        GameState g = new GameState(3, new java.util.Random());
        g.applyCardEffect(Card.of("R5"));
        assertEqual("normal card advances turn once", 1, g.getCurrentPlayer());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    static GameState gameWith(String upCode, String called) {
        GameState g = new GameState(2, new java.util.Random());
        g.setUpCard(Card.of(upCode));
        g.setCalledColor(called);
        return g;
    }

    static ArrayList<Card> clist(String... codes) {
        ArrayList<Card> list = new ArrayList<Card>();
        for (String c : codes) list.add(Card.of(c));
        return list;
    }

    static void assertTrue(String name, boolean v) {
        if (v) passed++;
        else { failed++; System.out.println("FAIL: " + name + " (expected true)"); }
    }

    static void assertFalse(String name, boolean v) {
        if (!v) passed++;
        else { failed++; System.out.println("FAIL: " + name + " (expected false)"); }
    }

    static void assertEqual(String name, int expected, int actual) {
        if (expected == actual) passed++;
        else { failed++; System.out.println("FAIL: " + name + " expected=" + expected + " actual=" + actual); }
    }

    static void assertEqual(String name, String expected, String actual) {
        if (expected.equals(actual)) passed++;
        else { failed++; System.out.println("FAIL: " + name + " expected=" + expected + " actual=" + actual); }
    }
}
