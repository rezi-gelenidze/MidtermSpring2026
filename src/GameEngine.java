import java.util.ArrayList;
import java.util.Random;

public class GameEngine {

    public static int playGame(int playerCount, Random random, GameListener listener) {
        GameState game = new GameState(playerCount, random);
        game.buildDeck();
        game.dealCards();

        int guard = 0;
        while (guard < 3000) {
            guard++;
            int cp = game.getCurrentPlayer();
            ArrayList<Card> hand = game.getHand(cp);

            listener.onTurnStart(cp, hand, game.getUpCard(), game.getCalledColor());

            int chosen = listener.chooseCard(cp, hand, game);

            if (chosen == -1) {
                Card drawn = game.draw();
                hand.add(drawn);
                listener.onCardDrawn(cp, drawn);
                if (Rules.isLegal(drawn, game.getUpCard(), game.getCalledColor())) {
                    chosen = hand.size() - 1;
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    listener.onInvalidIndex(cp);
                    hand.add(game.draw());
                    game.next();
                    continue;
                }

                Card card = hand.get(chosen);
                if (!Rules.isLegal(card, game.getUpCard(), game.getCalledColor())) {
                    listener.onIllegalPlay(cp, card);
                    hand.add(game.draw());
                    game.next();
                    continue;
                }

                hand.remove(chosen);
                game.addToDiscard(game.getUpCard());
                game.setUpCard(card);
                game.setCalledColor("");
                listener.onCardPlayed(cp, card);

                if (card.isWild()) {
                    String color = listener.chooseColor(cp, hand, game);
                    game.setCalledColor(color);
                }

                if (hand.size() == 1) {
                    listener.onUno(cp);
                }

                if (hand.size() == 0) {
                    int points = game.tallyPoints(cp);
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
        listener.onGameStopped();
        return 0;
    }
}
