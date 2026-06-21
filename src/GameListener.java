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
}
