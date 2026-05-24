# Extension Readiness Report

## Best candidate extension: smarter bot strategy

The highest-value, lowest-risk extension is replacing the current fixed-priority
bot with a pluggable strategy (e.g. one that tracks which colors opponents have
played and adapts).

### Why it is now tractable

After the refactoring:

- **`Card` is a proper value object.**  A strategy implementation can inspect
  `card.rank()`, `card.color()`, `card.number()`, `card.points()`, and
  `card.isWild()` without parsing strings or calling static helpers.

- **`Rules.isLegal` is the single source of truth.**  A strategy knows exactly
  how to test playability for any candidate card without duplicating conditions.

- **`chooseBotCard` is already isolated.**  It is a self-contained static method
  with a clear signature: it receives a hand (`ArrayList<Card>`) and returns an
  index.  Adding a `BotStrategy` interface requires changing only this call site
  in `Main`.

### Where the change goes

1. **New interface `src/BotStrategy.java`:**
   ```java
   public interface BotStrategy {
       /** Returns the index of the card to play, or -1 to draw. */
       int chooseCard(java.util.List<Card> hand, Card upCard, String calledColor);
       /** Returns the color to call after playing a wild. */
       String chooseColor(java.util.List<Card> hand);
   }
   ```

2. **Existing behaviour preserved as `src/SimpleBot.java`:**
   Move the current DRAW_TWO > SKIP > NUMBER > WILD priority logic into
   `SimpleBot implements BotStrategy`.

3. **New `src/AdaptiveBot.java`** (or any other strategy) can be plugged in
   independently.

4. **`Main.java` change is minimal:**
   Replace the `chooseBotCard(hand)` and `chooseBotColor(hand)` static calls with
   `strategy.chooseCard(hand, upCard, calledColor)` and
   `strategy.chooseColor(hand)`.  The rest of the game loop is untouched.

5. **`CharacterizationTest` stays green** because the existing `Main.chooseBotCard`
   static method can delegate to `new SimpleBot()`, keeping all 59 assertions
   passing.

## What still resists change

- **Static player/score state in `Main`.**  Player names, human/bot flags, and
  scores remain as static fields on `Main`.  These are CLI concerns and stay there,
  but multiple games in one run still share these globals (tests or extensions
  running games back-to-back must reset them).

- **`playGame()` still owns I/O.**  Console output is interleaved with calling
  `GameState` methods.  A GUI front-end or AI harness that wants to observe game
  events without printing to stdout would need `playGame()` refactored to fire
  callbacks or yield structured events instead of printing.

- **GameState fields are private with getters/setters.**  Game state (deck,
  discard, hands, upCard, calledColor, currentPlayer, direction) is now safely
  encapsulated in `GameState`, preventing invalid state transitions and allowing
  simulation/testing without side effects.  Multiple game instances can coexist
  without interference.

## Summary table

| Extension | Ready now? | Blocker if not |
|-----------|------------|----------------|
| Smarter bot strategy | Yes – add `BotStrategy` interface | None |
| Headless simulation / AI tournament | Partial – `Rules` and `Card` are clean | `playGame()` still prints; state is global |
| GUI front-end | No | I/O is inlined; no event/observer hook |
| Network multiplayer | No | All state is static; no session boundary |
