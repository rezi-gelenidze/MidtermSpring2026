import java.util.ArrayList;

public interface GameListener {
    int chooseCard(int player, ArrayList<Card> hand, GameState state);
    String chooseColor(int player, ArrayList<Card> hand, GameState state);
    void onTurnStart(int player, ArrayList<Card> hand, Card upCard, String calledColor);
    void onCardPlayed(int player, Card card);
    void onCardDrawn(int player, Card drawn);
    void onIllegalPlay(int player, Card card);
    void onInvalidIndex(int player);
    void onUno(int player);
    void onWin(int player, int points);
    void onForcedDraw(int target, int count);
    void onGameStopped();

    /** Asks whether the player calls UNO at the moment their hand reaches one card. */
    default boolean declareUno(int player, ArrayList<Card> hand, GameState state) {
        return state.chooseBotCallUno();
    }

    /** Fired when a player is caught not having called UNO and draws a penalty. */
    default void onMissedUnoPenalty(int player, int count) {}
}
