public class Rules {
    private Rules() {}

    public static boolean isLegal(Card card, Card upCard, String calledColor) {
        if (card.isWild()) return true;
        if (card.color().equals(upCard.color())) return true;
        if (!calledColor.isEmpty() && card.color().equals(calledColor)) return true;
        if (card.rank().equals(upCard.rank()) && !card.rank().equals("NUMBER")) return true;
        if (card.rank().equals("NUMBER") && upCard.rank().equals("NUMBER")
                && card.number() == upCard.number()) return true;
        return false;
    }
}
