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

- **Static global state in `Main`.**  There is no `GameState` class; fields like
  `deck`, `discard`, `hands`, `scores`, `currentPlayer`, and `direction` live as
  statics on `Main`.  Any extension that needs to simulate or fork game state
  (e.g. a look-ahead bot) cannot do so without a deeper restructuring.

- **`playGame()` owns both I/O and rules.**  Console output is interleaved with
  rule application.  A GUI front-end or AI harness that wants to observe game
  events without printing to stdout would need the game loop extracted into a
  separate class that fires callbacks or returns structured events.

- **Multi-game score tracking** is tied to a `scores[]` array indexed by player
  position.  Adding players or removing them between games without resetting the
  array is error-prone.

## Summary table

| Extension | Ready now? | Blocker if not |
|-----------|------------|----------------|
| Smarter bot strategy | Yes – add `BotStrategy` interface | None |
| Headless simulation / AI tournament | Partial – `Rules` and `Card` are clean | `playGame()` still prints; state is global |
| GUI front-end | No | I/O is inlined; no event/observer hook |
| Network multiplayer | No | All state is static; no session boundary |
