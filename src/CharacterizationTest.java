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
        assertEqual("color R5", "R", Main.color("R5"));
        assertEqual("color Y3", "Y", Main.color("Y3"));
        assertEqual("color G+2", "G", Main.color("G+2"));
        assertEqual("color BS", "B", Main.color("BS"));
        assertEqual("color W", "", Main.color("W"));
        assertEqual("color W4", "", Main.color("W4"));
    }

    static void testRank() {
        assertEqual("rank W", "WILD", Main.rank("W"));
        assertEqual("rank W4", "WILD_DRAW_FOUR", Main.rank("W4"));
        assertEqual("rank RS", "SKIP", Main.rank("RS"));
        assertEqual("rank GR", "REVERSE", Main.rank("GR"));
        assertEqual("rank B+2", "DRAW_TWO", Main.rank("B+2"));
        assertEqual("rank R0", "NUMBER", Main.rank("R0"));
        assertEqual("rank Y7", "NUMBER", Main.rank("Y7"));
    }

    static void testNumber() {
        assertEqual("number R0", 0, Main.number("R0"));
        assertEqual("number G9", 9, Main.number("G9"));
        assertEqual("number W", -1, Main.number("W"));
        assertEqual("number RS", -1, Main.number("RS"));
    }

    static void testScoringNumberCard() {
        assertEqual("points R5", 5, Main.points("R5"));
        assertEqual("points Y0", 0, Main.points("Y0"));
        assertEqual("points G9", 9, Main.points("G9"));
    }

    static void testScoringActionCard() {
        assertEqual("points GS", 20, Main.points("GS"));
        assertEqual("points BR", 20, Main.points("BR"));
        assertEqual("points G+2", 20, Main.points("G+2"));
    }

    static void testScoringWildCard() {
        assertEqual("points W", 50, Main.points("W"));
        assertEqual("points W4", 50, Main.points("W4"));
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
        Main.upCard = "R9"; Main.calledColor = "";
        ArrayList<String> hand = slist("RS", "R+2", "W");
        assertEqual("bot prefers DRAW_TWO", 1, Main.chooseBotCard(hand));
    }

    static void testChooseBotCardPrefersSkipOverNumber() {
        Main.upCard = "R9"; Main.calledColor = "";
        ArrayList<String> hand = slist("R5", "RS", "W");
        assertEqual("bot prefers SKIP over NUMBER", 1, Main.chooseBotCard(hand));
    }

    static void testChooseBotCardPrefersNumberOverWild() {
        Main.upCard = "R9"; Main.calledColor = "";
        ArrayList<String> hand = slist("B3", "R4", "W");
        assertEqual("bot prefers NUMBER over WILD", 1, Main.chooseBotCard(hand));
    }

    static void testChooseBotCardUsesWildAsLastResort() {
        Main.upCard = "R9"; Main.calledColor = "";
        ArrayList<String> hand = slist("G3", "B5", "W");
        assertEqual("bot uses WILD as last resort", 2, Main.chooseBotCard(hand));
    }

    static void testChooseBotCardReturnsMinusOneWhenNoPlay() {
        Main.upCard = "R9"; Main.calledColor = "";
        ArrayList<String> hand = slist("G3", "B5");
        assertEqual("bot returns -1 when no play", -1, Main.chooseBotCard(hand));
    }

    static void testChooseBotColor() {
        ArrayList<String> hand = slist("B1", "B2", "R3");
        assertEqual("chooseBotColor picks most common", "B", Main.chooseBotColor(hand));
    }

    static void testDrawReshuffle() {
        Main.deck.clear();
        Main.discard.clear();
        Main.discard.add("R1");
        Main.discard.add("G5");
        Main.random = new java.util.Random(42);
        String drawn = Main.draw();
        assertTrue("draw reshuffles discard when deck empty",
                drawn.equals("R1") || drawn.equals("G5"));
        assertTrue("discard cleared after reshuffle", Main.discard.size() == 0);
    }

    static ArrayList<String> slist(String... codes) {
        ArrayList<String> list = new ArrayList<String>();
        for (String c : codes) list.add(c);
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
