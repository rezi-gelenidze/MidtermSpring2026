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
        assertTrue("color match R2 on R9", Main.isLegal("R2", "R9", ""));
        assertTrue("color match GS on G3", Main.isLegal("GS", "G3", ""));
    }

    static void testIsLegalNumberMatch() {
        assertTrue("number match G9 on R9", Main.isLegal("G9", "R9", ""));
        assertTrue("number match B0 on Y0", Main.isLegal("B0", "Y0", ""));
    }

    static void testIsLegalActionMatch() {
        assertTrue("skip match BS on RS", Main.isLegal("BS", "RS", ""));
        assertTrue("reverse match YR on GR", Main.isLegal("YR", "GR", ""));
        assertTrue("draw-two match R+2 on G+2", Main.isLegal("R+2", "G+2", ""));
    }

    static void testIsLegalWildAlwaysLegal() {
        assertTrue("W legal on R5", Main.isLegal("W", "R5", ""));
        assertTrue("W4 legal on BS", Main.isLegal("W4", "BS", ""));
        assertTrue("W legal on W up-card", Main.isLegal("W", "W", ""));
    }

    static void testIsLegalCalledColor() {
        assertTrue("called B: B3 on W", Main.isLegal("B3", "W", "B"));
        assertFalse("called Y: R5 on W", Main.isLegal("R5", "W", "Y"));
    }

    static void testIsLegalIllegalMismatch() {
        assertFalse("mismatch B3 on R9", Main.isLegal("B3", "R9", ""));
        assertFalse("mismatch GS on R9", Main.isLegal("GS", "R9", ""));
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
