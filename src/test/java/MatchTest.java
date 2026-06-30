import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the pure match-level helpers used to run rounds until a target
 * score is reached. These have no console dependency — Main.java's loop
 * is the only console-facing caller.
 */
public class MatchTest {

    @Test void testReachedTargetTrueWhenAnyScoreAtOrAboveTarget() {
        assertTrue(Match.reachedTarget(new int[]{100, 500, 200}, 500));
        assertTrue(Match.reachedTarget(new int[]{600}, 500));
    }

    @Test void testReachedTargetFalseWhenAllScoresBelowTarget() {
        assertFalse(Match.reachedTarget(new int[]{100, 200, 300}, 500));
    }

    @Test void testReachedTargetWithZeroTargetIsImmediatelyMet() {
        // Documents why Main.java uses a do-while loop: a target of 0 (or
        // negative) is satisfied by an all-zero starting score, so the
        // round-continuation check alone must not be allowed to skip the
        // very first round.
        assertTrue(Match.reachedTarget(new int[]{0, 0, 0}, 0));
    }

    @Test void testDetermineWinnerPicksHighestScore() {
        assertEquals(2, Match.determineWinner(new int[]{100, 300, 400}));
    }

    @Test void testDetermineWinnerBreaksTiesByLowestIndex() {
        assertEquals(0, Match.determineWinner(new int[]{300, 300, 100}));
    }
}
