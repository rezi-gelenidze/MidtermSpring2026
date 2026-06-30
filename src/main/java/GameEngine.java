import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameEngine {

    private static final Logger log = LoggerFactory.getLogger(GameEngine.class);

    public static int playGame(int playerCount, Random random, GameListener listener) {
        GameState game = new GameState(playerCount, random);
        game.buildDeck();
        game.dealCards();
        log.info("Game started with {} players", playerCount);
        return playGame(game, listener);
    }

    /** Runs the turn loop on an already-built/dealt GameState — lets tests rig state directly. */
    public static int playGame(GameState game, GameListener listener) {
        int guard = 0;
        while (guard < 3000) {
            guard++;

            for (int penalized : game.checkMissedUnoPenalties()) {
                listener.onMissedUnoPenalty(penalized, 2);
            }

            int cp = game.getCurrentPlayer();
            ArrayList<Card> hand = game.getHand(cp);

            log.debug("Player {} turn — hand size: {}, up card: {}", cp, hand.size(), game.getUpCard());

            listener.onTurnStart(cp, hand, game.getUpCard(), game.getCalledColor());

            int chosen = listener.chooseCard(cp, hand, game);

            if (chosen == -1) {
                Card drawn = game.draw();
                hand.add(drawn);
                log.debug("Player {} drew {}", cp, drawn);
                listener.onCardDrawn(cp, drawn);
                if (Rules.isLegal(drawn, game.getUpCard(), game.getCalledColor())) {
                    chosen = hand.size() - 1;
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    log.warn("Player {} selected invalid index {}", cp, chosen);
                    listener.onInvalidIndex(cp);
                    hand.add(game.draw());
                    game.next();
                    continue;
                }

                Card card = hand.get(chosen);
                if (!Rules.isLegal(card, game.getUpCard(), game.getCalledColor())) {
                    log.warn("Player {} attempted illegal card {} on {}", cp, card, game.getUpCard());
                    listener.onIllegalPlay(cp, card);
                    hand.add(game.draw());
                    game.next();
                    continue;
                }

                hand.remove(chosen);
                game.addToDiscard(game.getUpCard());
                game.setUpCard(card);
                game.setCalledColor("");
                log.info("Player {} played {}", cp, card);
                listener.onCardPlayed(cp, card);

                if (card.isWild()) {
                    String color = listener.chooseColor(cp, hand, game);
                    game.setCalledColor(color);
                    log.info("Player {} called color {}", cp, color);
                }

                if (hand.size() == 1) {
                    boolean called = listener.declareUno(cp, hand, game);
                    game.resolveUnoDeclaration(cp, called);
                    if (called) {
                        listener.onUno(cp);
                    }
                }

                if (hand.size() == 0) {
                    int points = game.tallyPoints(cp);
                    log.info("Game ended — player {} wins with {} points", cp, points);
                    listener.onWin(cp, points);
                    return points;
                }

                game.applyCardEffect(card);

                if (game.getLastForcedDrawCount() > 0) {
                    listener.onForcedDraw(game.getLastForcedDrawTarget(), game.getLastForcedDrawCount());
                }

            } else {
                game.next();
            }
        }
        log.warn("Game stopped at safety limit ({} turns)", guard);
        listener.onGameStopped();
        return 0;
    }
}
