import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Main is the CLI entry point.  It owns only console I/O concerns:
 * reading player input, printing game events, and accumulating scores
 * across games.  All game logic lives in GameState; round orchestration
 * lives in GameEngine.
 */
public class Main {
    static ArrayList<String>  playerNames  = new ArrayList<String>();
    static ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    static int[]   scores  = new int[10];
    static boolean quiet   = false;
    static Random  random  = new Random();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int bots = 3;
        int games = 1;
        boolean human = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length) {
                bots = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--games") && i + 1 < args.length) {
                games = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                quiet = true;
            } else if (args[i].equals("--seed") && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if (args[i].equals("--self-test")) {
                selfTest();
                return;
            } else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        random = new Random(seed);
        setupPlayers(bots, human);

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        for (int g = 1; g <= games; g++) {
            if (!quiet) System.out.println("\n=== Game " + g + " ===");
            playGame();
        }

        System.out.println("\nFinal scores:");
        for (int i = 0; i < playerNames.size(); i++) {
            System.out.println(playerNames.get(i) + ": " + scores[i]);
        }
    }

    static void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
        }
    }

    static void playGame() {
        GameListener listener = new GameListener() {
            public int chooseCard(int player, ArrayList<Card> hand, GameState game) {
                boolean isHuman = humanPlayers.get(player).booleanValue();
                return isHuman ? askHuman(hand, game) : game.chooseBotCard(hand);
            }

            public String chooseColor(int player, ArrayList<Card> hand, GameState game) {
                boolean isHuman = humanPlayers.get(player).booleanValue();
                return isHuman ? askColor() : game.chooseBotColor(hand);
            }

            public void onTurnStart(int player, ArrayList<Card> hand, Card upCard, String calledColor) {
                if (!quiet) {
                    System.out.println("\nUp card: " + upCard
                            + (calledColor.equals("") ? "" : " called " + calledColor));
                    System.out.println(playerNames.get(player) + " hand: " + join(hand));
                }
            }

            public void onCardPlayed(int player, Card card) {
                if (!quiet) System.out.println(playerNames.get(player) + " plays " + card);
                if (card.isWild() && !quiet) {
                    // color announcement handled after chooseColor returns
                }
            }

            public void onCardDrawn(int player, Card drawn) {
                if (!quiet) System.out.println(playerNames.get(player) + " draws " + drawn);
            }

            public void onIllegalPlay(int player, Card card) {
                if (!quiet) System.out.println(playerNames.get(player) + " tried illegal card " + card + " and draws a penalty card.");
            }

            public void onInvalidIndex(int player) {
                if (!quiet) System.out.println(playerNames.get(player) + " selected an invalid index and draws a penalty card.");
            }

            public void onUno(int player) {
                if (!quiet) System.out.println(playerNames.get(player) + " says UNO!");
            }

            public void onWin(int player, int points) {
                scores[player] += points;
                if (!quiet) System.out.println(playerNames.get(player) + " wins and scores " + points);
            }

            public void onForcedDraw(int target, int count) {
                if (!quiet) {
                    String word = count == 2 ? "two" : "four";
                    System.out.println(playerNames.get(target) + " draws " + word + ".");
                }
            }

            public void onGameStopped() {
                if (!quiet) System.out.println("Game stopped at safety limit.");
            }
        };

        GameEngine.playGame(playerNames.size(), random, listener);
    }

    static int askHuman(ArrayList<Card> hand, GameState game) {
        while (true) {
            System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) return -1;
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) return index;
            } catch (NumberFormatException ignored) {}
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).code.equals(input)) {
                    if (Rules.isLegal(hand.get(i), game.getUpCard(), game.getCalledColor())) return i;
                    System.out.println("That card is not legal.");
                }
            }
            System.out.println("Card not found.");
        }
    }

    static String askColor() {
        while (true) {
            System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R") || input.equals("Y") || input.equals("G") || input.equals("B")) return input;
            System.out.println("Bad color.");
        }
    }

    static String join(ArrayList<Card> cards) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            out.append(i).append(':').append(cards.get(i));
            if (i < cards.size() - 1) out.append(' ');
        }
        return out.toString();
    }

    // Shims kept for selfTest() backward compatibility
    static boolean isLegal(String card, String up, String call) {
        return Rules.isLegal(Card.of(card), Card.of(up), call);
    }
    static String color(String card)  { return Card.of(card).color(); }
    static String rank(String card)   { return Card.of(card).rank(); }
    static int    number(String card) { return Card.of(card).number(); }
    static int    points(String card) { return Card.of(card).points(); }

    static void selfTest() {
        int passed = 0;
        if (color("R5").equals("R")) passed++; else fail("color R5");
        if (rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (points("W4") == 50) passed++; else fail("wild points");
        if (isLegal("R2", "R9", "")) passed++; else fail("same color");
        if (isLegal("G9", "R9", "")) passed++; else fail("same number");
        if (isLegal("B3", "W", "B")) passed++; else fail("called color");
        if (!isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");

        GameState g = new GameState(2, random);
        g.setUpCard(Card.of("R9")); g.setCalledColor("");
        ArrayList<Card> h = new ArrayList<Card>();
        h.add(Card.of("B3")); h.add(Card.of("R4")); h.add(Card.of("W"));
        if (g.chooseBotCard(h) == 1) passed++; else fail("bot normal before wild");

        ArrayList<Card> h2 = new ArrayList<Card>();
        h2.add(Card.of("B1")); h2.add(Card.of("B2")); h2.add(Card.of("R3"));
        if (g.chooseBotColor(h2).equals("B")) passed++; else fail("bot color");

        System.out.println("Passed " + passed + " characterization checks.");
    }

    static void fail(String name) { throw new RuntimeException("Failed: " + name); }
}
