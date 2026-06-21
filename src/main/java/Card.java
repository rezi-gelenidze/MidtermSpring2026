public class Card {
    public final String code;

    private Card(String code) { this.code = code; }

    public static Card of(String code) { return new Card(code); }

    public String color() {
        if (code.startsWith("R")) return "R";
        if (code.startsWith("Y")) return "Y";
        if (code.startsWith("G")) return "G";
        if (code.startsWith("B")) return "B";
        return "";
    }

    public String rank() {
        if (code.equals("W"))   return "WILD";
        if (code.equals("W4"))  return "WILD_DRAW_FOUR";
        if (code.endsWith("S")) return "SKIP";
        if (code.endsWith("R")) return "REVERSE";
        if (code.endsWith("+2")) return "DRAW_TWO";
        return "NUMBER";
    }

    public int number() {
        if (rank().equals("NUMBER")) return Integer.parseInt(code.substring(1));
        return -1;
    }

    public int points() {
        String r = rank();
        if (r.equals("NUMBER")) return number();
        if (r.equals("SKIP") || r.equals("REVERSE") || r.equals("DRAW_TWO")) return 20;
        if (r.equals("WILD") || r.equals("WILD_DRAW_FOUR")) return 50;
        return 0;
    }

    public boolean isWild() { return code.startsWith("W"); }

    @Override public String toString() { return code; }

    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Card)) return false;
        return code.equals(((Card) obj).code);
    }

    @Override public int hashCode() { return code.hashCode(); }
}
