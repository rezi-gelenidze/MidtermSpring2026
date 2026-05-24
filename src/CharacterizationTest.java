import java.util.ArrayList;

/**
 * Characterization tests pinning the existing behaviour of Main.java.
 * Written before any refactoring so they act as a safety net.
 *
 * Quirks intentionally preserved (documented, some testable below):
 *  1. All hands are printed to console on every turn (no hidden information).
 *  2. A human player may type DRAW even when legal cards are in hand; the
 *     engine never forces a play on the human's behalf.
 *  3. If chooseBotCard returns an index >= hand.size() the engine adds a
 *     penalty card and advances the turn (safety guard, not normally reachable).
 *  4. Bots automatically play a drawn card that turns out to be legal.
 */
public class CharacterizationTest {
    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        testColor();
        testRank();
        testNumber();
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
        testDrawReshuffle();
        testCardIsWild();
        testTallyPoints();
        testBotAutoPlayDrawn();
        testChooseBotColorTieBreak();

        System.out.println("\nCharacterizationTest: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) System.exit(1);
    }

    static void testColor() {
        assertEqual("color R5", "R", Card.of("R5").color());
        assertEqual("color Y3", "Y", Card.of("Y3").color());
        assertEqual("color G+2", "G", Card.of("G+2").color());
        assertEqual("color BS", "B", Card.of("BS").color());
        assertEqual("color W", "", Card.of("W").color());
        assertEqual("color W4", "", Card.of("W4").color());
    }

    static void testRank() {
        assertEqual("rank W", "WILD", Card.of("W").rank());
        assertEqual("rank W4", "WILD_DRAW_FOUR", Card.of("W4").rank());
        assertEqual("rank RS", "SKIP", Card.of("RS").rank());
        assertEqual("rank GR", "REVERSE", Card.of("GR").rank());
        assertEqual("rank B+2", "DRAW_TWO", Card.of("B+2").rank());
        assertEqual("rank R0", "NUMBER", Card.of("R0").rank());
        assertEqual("rank Y7", "NUMBER", Card.of("Y7").rank());
    }

    static void testNumber() {
        assertEqual("number R0", 0, Card.of("R0").number());
        assertEqual("number G9", 9, Card.of("G9").number());
        assertEqual("number W", -1, Card.of("W").number());
        assertEqual("number RS", -1, Card.of("RS").number());
    }

    static void testScoringNumberCard() {
        assertEqual("points R5", 5, Card.of("R5").points());
        assertEqual("points Y0", 0, Card.of("Y0").points());
        assertEqual("points G9", 9, Card.of("G9").points());
    }

    static void testScoringActionCard() {
        assertEqual("points GS", 20, Card.of("GS").points());
        assertEqual("points BR", 20, Card.of("BR").points());
        assertEqual("points G+2", 20, Card.of("G+2").points());
    }

    static void testScoringWildCard() {
        assertEqual("points W", 50, Card.of("W").points());
        assertEqual("points W4", 50, Card.of("W4").points());
    }

    static void testIsLegalColorMatch() {
        assertTrue("color match R2 on R9", Rules.isLegal(Card.of("R2"), Card.of("R9"), ""));
        assertTrue("color match GS on G3", Rules.isLegal(Card.of("GS"), Card.of("G3"), ""));
    }

    static void testIsLegalNumberMatch() {
        assertTrue("number match G9 on R9", Rules.isLegal(Card.of("G9"), Card.of("R9"), ""));
        assertTrue("number match B0 on Y0", Rules.isLegal(Card.of("B0"), Card.of("Y0"), ""));
    }

    static void testIsLegalActionMatch() {
        assertTrue("skip match BS on RS", Rules.isLegal(Card.of("BS"), Card.of("RS"), ""));
        assertTrue("reverse match YR on GR", Rules.isLegal(Card.of("YR"), Card.of("GR"), ""));
        assertTrue("draw-two match R+2 on G+2", Rules.isLegal(Card.of("R+2"), Card.of("G+2"), ""));
    }

    static void testIsLegalWildAlwaysLegal() {
        assertTrue("W legal on R5", Rules.isLegal(Card.of("W"), Card.of("R5"), ""));
        assertTrue("W4 legal on BS", Rules.isLegal(Card.of("W4"), Card.of("BS"), ""));
        assertTrue("W legal on W up-card", Rules.isLegal(Card.of("W"), Card.of("W"), ""));
    }

    static void testIsLegalCalledColor() {
        assertTrue("called B: B3 on W", Rules.isLegal(Card.of("B3"), Card.of("W"), "B"));
        assertFalse("called Y: R5 on W", Rules.isLegal(Card.of("R5"), Card.of("W"), "Y"));
    }

    static void testIsLegalIllegalMismatch() {
        assertFalse("mismatch B3 on R9", Rules.isLegal(Card.of("B3"), Card.of("R9"), ""));
        assertFalse("mismatch GS on R9", Rules.isLegal(Card.of("GS"), Card.of("R9"), ""));
    }

    static void testChooseBotCardPrefersDrawTwo() {
        Main.upCard = Card.of("R9"); Main.calledColor = "";
        ArrayList<Card> hand = clist("RS", "R+2", "W");
        assertEqual("bot prefers DRAW_TWO", 1, Main.chooseBotCard(hand));
    }

    static void testChooseBotCardPrefersSkipOverNumber() {
        Main.upCard = Card.of("R9"); Main.calledColor = "";
        ArrayList<Card> hand = clist("R5", "RS", "W");
        assertEqual("bot prefers SKIP over NUMBER", 1, Main.chooseBotCard(hand));
    }

    static void testChooseBotCardPrefersNumberOverWild() {
        Main.upCard = Card.of("R9"); Main.calledColor = "";
        ArrayList<Card> hand = clist("B3", "R4", "W");
        assertEqual("bot prefers NUMBER over WILD", 1, Main.chooseBotCard(hand));
    }

    static void testChooseBotCardUsesWildAsLastResort() {
        Main.upCard = Card.of("R9"); Main.calledColor = "";
        ArrayList<Card> hand = clist("G3", "B5", "W");
        assertEqual("bot uses WILD as last resort", 2, Main.chooseBotCard(hand));
    }

    static void testChooseBotCardReturnsMinusOneWhenNoPlay() {
        Main.upCard = Card.of("R9"); Main.calledColor = "";
        ArrayList<Card> hand = clist("G3", "B5");
        assertEqual("bot returns -1 when no play", -1, Main.chooseBotCard(hand));
    }

    static void testChooseBotColor() {
        ArrayList<Card> hand = clist("B1", "B2", "R3");
        assertEqual("chooseBotColor picks most common", "B", Main.chooseBotColor(hand));
    }

    static void testDrawReshuffle() {
        Main.deck.clear();
        Main.discard.clear();
        Main.discard.add(Card.of("R1"));
        Main.discard.add(Card.of("G5"));
        Main.random = new java.util.Random(42);
        Card drawn = Main.draw();
        assertTrue("draw reshuffles discard when deck empty",
                drawn.code.equals("R1") || drawn.code.equals("G5"));
        assertTrue("discard cleared after reshuffle", Main.discard.size() == 0);
    }

    static void testCardIsWild() {
        assertTrue("W isWild", Card.of("W").isWild());
        assertTrue("W4 isWild", Card.of("W4").isWild());
        assertFalse("R5 not wild", Card.of("R5").isWild());
        assertFalse("GS not wild", Card.of("GS").isWild());
    }

    static void testTallyPoints() {
        // 3 players: winner=0, p1 holds R5+GS=25, p2 holds W+B3=53 => total 78
        Main.hands.clear();
        ArrayList<Card> p0 = new ArrayList<Card>(); // winner, not counted
        ArrayList<Card> p1 = new ArrayList<Card>();
        p1.add(Card.of("R5")); p1.add(Card.of("GS"));
        ArrayList<Card> p2 = new ArrayList<Card>();
        p2.add(Card.of("W")); p2.add(Card.of("B3"));
        Main.hands.add(p0); Main.hands.add(p1); Main.hands.add(p2);
        assertEqual("tallyPoints sums opponents", 78, Main.tallyPoints(0));
    }

    static void testBotAutoPlayDrawn() {
        // Pins the auto-play component: when bot draws a legal card, isLegal is true
        Main.upCard = Card.of("R9");
        Main.calledColor = "";
        Main.deck.clear();
        Main.deck.add(Card.of("R5"));  // first card in deck
        Card drawn = Main.draw();
        assertTrue("drawn card is legal (enables bot auto-play)",
                Rules.isLegal(drawn, Main.upCard, Main.calledColor));
        assertEqual("drawn card code", "R5", drawn.code);
    }

    static void testChooseBotColorTieBreak() {
        // equal count of all colors; result must be a valid color
        ArrayList<Card> hand = clist("R1", "G2", "Y3", "B4");
        String color = Main.chooseBotColor(hand);
        assertTrue("chooseBotColor returns valid color",
                color.equals("R") || color.equals("Y") || color.equals("G") || color.equals("B"));
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
