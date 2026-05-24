# Refactoring Report

## What behaviour was characterised before refactoring

Before any structural change was made, `CharacterizationTest.java` was written to
pin the following behaviour at the method level (no CLI required):

| Area | What was tested |
|------|----------------|
| `Card` value object | color(), rank(), number(), points(), isWild() for all card types |
| `Rules.isLegal` | color match, number match, action-rank match, wild always legal, calledColor override, illegal mismatches |
| `chooseBotCard` | preference order DRAW_TWO > SKIP > NUMBER > WILD; -1 when no play |
| `chooseBotColor` | returns the color with the most cards in hand |
| Scoring | NUMBER = face value, action = 20, wild = 50 |
| `draw()` | reshuffles discard pile into deck when deck is empty |

The original `--self-test` path (9 inline checks in `Main.selfTest()`) was preserved
and continues to pass.

## Worst design problems found

1. **Duplicated legality logic.** The exact same five-condition block appeared in
   `isLegal()` and was copy-pasted five times inside `chooseBotCard()` (once per
   priority loop).  Any rule change required updating six locations consistently.

2. **Primitive obsession — cards as bare Strings.** `color()`, `rank()`,
   `number()`, and `points()` were static helpers in `Main` that all took a
   `String`.  There was no type to ask about a card; callers had to remember which
   helper to call and pass the right string format.

3. **I/O mixed into game logic.** `playGame()` was 190 lines combining deck
   management, legality checks, turn advancement, card effects, scoring, console
   output, and Scanner input.  None of the game logic could be tested without
   producing console output or reading stdin.

4. **All state as static fields on Main.** `upCard`, `calledColor`, `deck`,
   `discard`, `hands`, `currentPlayer`, `direction` are class-level statics.
   Tests must mutate global state; there is no isolation between test cases.

## Refactorings performed

### Step 1 – CharacterizationTest (before any other change)
Added `src/CharacterizationTest.java` with 59 assertions across all functional
areas.  Updated `scripts/test.sh` to run it.  No production code was touched at
this step.

### Step 2 – Extract `Card.java`
Created `src/Card.java` as an immutable value object wrapping a code string.
Moved `color()`, `rank()`, `number()`, `points()`, and `isWild()` onto the class.
Migrated `Main`'s `hands`, `deck`, `discard`, and `upCard` fields from
`ArrayList<String>` / `String` to `ArrayList<Card>` / `Card`.  The static helpers
on `Main` were kept as thin delegating wrappers so that `selfTest()` continued to
pass unchanged.

### Step 3 – Extract `Rules.java`
Created `src/Rules.java` containing the single public method
`Rules.isLegal(Card, Card, String)`.  Removed the five-times-duplicated inline
condition block from `chooseBotCard`; each priority loop now calls
`Rules.isLegal()` instead.  The `isLegal(String,String,String)` shim in `Main`
delegates to `Rules.isLegal`.

### Step 4 – Extract helper methods in `Main`
Extracted two focused methods from `playGame()`:
- `tallyPoints(int winner)` – sums opponent hand values; removes the nested loop
  from the win-detection branch.
- `applyCardEffect(Card, ArrayList<Card>)` – applies SKIP/REVERSE/DRAW_TWO/
  WILD_DRAW_FOUR side-effects and advances the turn; removes the long if-chain
  from inside `playGame()`.

`chooseBotCard` was also simplified: each priority loop is now a single readable
`if` after the `Rules.isLegal` call replaces the duplicated block.

## Behaviour intentionally preserved (including quirks)

- **Draw when legal plays exist (human).** A human player may type `DRAW` even
  when legal cards are in hand.  The engine draws a card and then offers to play
  it; it never forces a play.
- **Invalid index → penalty card.** If `chosen >= hand.size()` the player
  receives a penalty card and the turn advances; there is no re-prompt.
- **Bots auto-play drawn card.** When a bot draws and the drawn card is legal,
  `chosen` is set to the last index and the card is played immediately.
- **All hands visible.** Every player's hand is printed at the start of their
  turn regardless of whether they are human or bot.
- **Safety limit of 3000 turns.** The `guard` counter still terminates runaway
  games.

## Risks that remain

- **Static global state.** All game state (`deck`, `discard`, `hands`, `upCard`,
  `calledColor`, `currentPlayer`, `direction`, `scores`, `random`) is still held
  as static fields on `Main`.  Tests must carefully reset these fields; two test
  runs in the same JVM process can interfere.
- **`playGame()` is still long.** At ~150 lines it is much improved but still
  handles game setup, the main loop, human I/O, win detection, and card effects.
  A `GameState` or `Game` class would isolate it further.
- **No interface for bot strategy.** `chooseBotCard` is a static method.
  Plugging in an alternative strategy still requires modifying `Main`.
