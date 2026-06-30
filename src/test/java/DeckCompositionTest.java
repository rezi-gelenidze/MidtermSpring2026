import java.util.Random;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies GameState.buildDeck() produces a standard 108-card UNO deck:
 * four colors, one 0 and two of each 1-9, two Skip/Reverse/Draw Two per
 * color, and four Wild plus four Wild Draw Four.
 */
public class DeckCompositionTest {

    @Test void testDeckHasExactlyOneHundredAndEightCards() {
        GameState g = new GameState(2, new Random());
        g.buildDeck();
        assertEquals(108, g.getDeck().size());
    }

    @Test void testDeckHasCorrectPerColorComposition() {
        GameState g = new GameState(2, new Random());
        g.buildDeck();

        String[] colors = {"R", "Y", "G", "B"};
        for (String color : colors) {
            int zeros = 0, numbers = 0, skips = 0, reverses = 0, drawTwos = 0;
            for (Card c : g.getDeck()) {
                if (!c.color().equals(color)) continue;
                if (c.rank().equals("NUMBER") && c.number() == 0) zeros++;
                else if (c.rank().equals("NUMBER")) numbers++;
                else if (c.rank().equals("SKIP")) skips++;
                else if (c.rank().equals("REVERSE")) reverses++;
                else if (c.rank().equals("DRAW_TWO")) drawTwos++;
            }
            assertEquals(1, zeros, color + " should have exactly one 0 card");
            assertEquals(18, numbers, color + " should have two each of 1-9");
            assertEquals(2, skips, color + " should have two Skip cards");
            assertEquals(2, reverses, color + " should have two Reverse cards");
            assertEquals(2, drawTwos, color + " should have two Draw Two cards");
        }
    }

    @Test void testDeckHasCorrectWildComposition() {
        GameState g = new GameState(2, new Random());
        g.buildDeck();

        long wilds = g.getDeck().stream().filter(c -> c.rank().equals("WILD")).count();
        long wildDrawFours = g.getDeck().stream().filter(c -> c.rank().equals("WILD_DRAW_FOUR")).count();

        assertEquals(4, wilds);
        assertEquals(4, wildDrawFours);
    }
}
