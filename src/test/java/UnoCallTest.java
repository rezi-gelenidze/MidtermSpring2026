import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the UNO-call / missed-call-penalty mechanic added in GameState.
 * Timing rule under test: a player must declare UNO at the moment their
 * hand reaches exactly one card (resolveUnoDeclaration). Anyone still at
 * one card without a declaration on the next scan (checkMissedUnoPenalties,
 * called once per turn for every player) draws a two-card penalty. The
 * declaration must be made again any time the hand size changes away from
 * one and later returns to one.
 */
public class UnoCallTest {

    @Test void testCallingUnoPreventsThePenalty() {
        GameState g = new GameState(2, new Random());
        g.buildDeck(); g.dealCards();

        g.getHand(0).clear();
        g.getHand(0).add(Card.of("R5"));
        g.resolveUnoDeclaration(0, true);

        List<Integer> penalized = g.checkMissedUnoPenalties();

        assertFalse(penalized.contains(0));
        assertEquals(1, g.getHand(0).size());
    }

    @Test void testMissingTheCallAppliesATwoCardPenalty() {
        GameState g = new GameState(2, new Random());
        g.buildDeck(); g.dealCards();

        g.getHand(0).clear();
        g.getHand(0).add(Card.of("R5"));
        // No call to resolveUnoDeclaration — the player forgot to call UNO.

        List<Integer> penalized = g.checkMissedUnoPenalties();

        assertTrue(penalized.contains(0));
        assertEquals(3, g.getHand(0).size());
    }

    @Test void testDeclarationMustBeRepeatedAfterHandSizeChanges() {
        GameState g = new GameState(2, new Random());
        g.buildDeck(); g.dealCards();

        g.getHand(0).clear();
        g.getHand(0).add(Card.of("R5"));
        g.resolveUnoDeclaration(0, true);
        assertFalse(g.checkMissedUnoPenalties().contains(0));

        // Hand grows back above one card (e.g. a forced draw) — flag resets.
        g.getHand(0).add(Card.of("B3"));
        g.checkMissedUnoPenalties();
        assertFalse(g.hasCalledUno(0));

        // Hand drops back to one card without a fresh declaration.
        g.getHand(0).remove(0);
        List<Integer> penalized = g.checkMissedUnoPenalties();

        assertTrue(penalized.contains(0), "must re-declare UNO after the hand size changed away from one");
        assertEquals(3, g.getHand(0).size());
    }

    @Test void testNoPenaltyWhenHandIsNotExactlyOneCard() {
        GameState g = new GameState(2, new Random());
        g.buildDeck(); g.dealCards();

        g.getHand(0).clear();
        g.getHand(0).add(Card.of("R5"));
        g.getHand(0).add(Card.of("B3"));

        List<Integer> penalized = g.checkMissedUnoPenalties();

        assertFalse(penalized.contains(0));
        assertEquals(2, g.getHand(0).size());
    }

    @Test void testChooseBotCallUnoIsDeterministicAndMostlyCallsUno() {
        GameState g = new GameState(2, new Random(1));

        int calls = 0;
        for (int i = 0; i < 1000; i++) {
            if (g.chooseBotCallUno()) calls++;
        }

        assertTrue(calls > 800, "bots should call UNO roughly 90% of the time");
        assertTrue(calls < 1000, "bots should occasionally forget to call UNO");
    }
}
