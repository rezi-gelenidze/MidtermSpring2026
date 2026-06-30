/**
 * Pure, console-free helpers for running a multi-round match to a target score.
 * Each round's score is accumulated by the caller (see Main.playGame()'s onWin
 * handler); this class only decides when the match is over and who won.
 */
public class Match {
    private Match() {}

    public static boolean reachedTarget(int[] scores, int target) {
        for (int s : scores) {
            if (s >= target) return true;
        }
        return false;
    }

    /** Highest score wins; ties are broken by the lowest player index. */
    public static int determineWinner(int[] scores) {
        int best = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[best]) best = i;
        }
        return best;
    }
}
