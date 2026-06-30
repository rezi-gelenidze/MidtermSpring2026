# Final Report

## What UNO rules are implemented

All ten rule-feature menu items from `Final_Project.md` are implemented:
correct deck composition, full legal-play validation, Skip, Reverse (with a
documented 2-player simplification), Draw Two, Wild, Wild Draw Four (no
challenge rule), draw/pass behavior, UNO call and missed-UNO penalty, and
round scoring with a multi-round match to a target score. See
`docs/rules-supported.md` for the rule-by-rule detail and every documented
simplification.

The first eight were already built during the midterm refactor. This final
project added the last two: a real UNO-call/penalty mechanic, and a
`--target` flag that turns the existing per-round scoring into a genuine
multi-round match with a declared winner.

## How the game is played from the CLI

```
./mvnw exec:java -Dexec.args="--human --bots 2 --games 50 --target 500"
```

Each round deals fresh hands and a starting discard card. On a human's turn,
the CLI lists the hand (`index:code`) and prompts `Choose card index/code or
draw:` — type a hand index, a card code (e.g. `R5`, `YS`, `G+2`, `W`), or
`draw`. Playing a Wild prompts `Call color R/Y/G/B:`. The moment a player's
hand drops to one card, the CLI prompts `Call UNO? (Y/N):`; missing the call
draws a two-card penalty before the player's next real turn. Bots play
automatically using a fixed-priority strategy. When `--target` is supplied,
rounds keep dealing until a player's cumulative score reaches the target (or
the `--games` safety cap is hit), and the CLI prints the declared match
winner. Without `--target`, the CLI behaves exactly as it always has: it
plays exactly `--games` rounds and prints final scores with no declared
winner.

## How the architecture separates game logic from CLI interaction

- `Card` — immutable card value object (color/rank/number/points/isWild).
- `Rules` — single source of truth for legal-play validation.
- `GameState` — owns all mutable round state (deck, discard, hands, up card,
  called color, direction, current player, UNO-declaration flags) and the
  pure rule operations on that state (`buildDeck`, `dealCards`, `draw`,
  `applyCardEffect`, `chooseBotCard`, `chooseBotColor`,
  `resolveUnoDeclaration`, `checkMissedUnoPenalties`, `chooseBotCallUno`).
  None of this performs I/O.
- `GameEngine` — runs one round's turn loop against a `GameState` and a
  `GameListener`, calling back into the listener for every decision point and
  every notification. Testable headlessly: `GameEngine.playGame(GameState,
  GameListener)` accepts an already-constructed `GameState`, so tests can rig
  exact scenarios (decks, hands, up cards) without any console involved.
- `GameListener` — the seam between rules and presentation. `Main.java`
  implements it once with real CLI prompts/printing; tests implement it with
  silent or scripted stand-ins. The two new hooks (`declareUno`,
  `onMissedUnoPenalty`) are Java `default` methods, so existing test
  listeners needed no changes at all.
- `Match` — pure, stateless helpers (`reachedTarget`, `determineWinner`) for
  the multi-round target-score decision. No dependency on `GameEngine`,
  `GameListener`, or `GameState` — directly unit-testable.
- `Main` — the only class that touches `Scanner`/`System.out`/CLI argument
  parsing/persistence. It owns the round-counting loop and accumulates
  scores via the `onWin` callback; it does not implement any rule logic
  itself.

This means every rule feature — including both newly added ones — is testable
without running the CLI, console input, or producing console output.

## What tests were added

- `DeckCompositionTest` — exact 108-card composition (per-color and overall).
- `DrawPassTest` — the illegal-drawn-card → pass path (complements the
  existing `testBotAutoPlayDrawn`, which covers the legal-drawn-card path).
- `UnoCallTest` — declaring UNO prevents the penalty; missing the call
  applies a two-card penalty; the declaration must be repeated after the hand
  size changes away from one and back; no penalty when the hand isn't
  exactly one card; the bot's 90%/10% call/forget rate is deterministic under
  a fixed seed.
- `MatchTest` — `reachedTarget` and `determineWinner`, including the
  target-0 edge case and the lowest-index tie-break rule.
- `testChooseBotCardPlaysReverseWhenItIsTheOnlyLegalCard` (added to
  `CharacterizationTest`) — a regression test for a pre-existing bug found
  during this work: `GameState.chooseBotCard` checked DRAW_TWO/SKIP/NUMBER/
  WILD but never REVERSE, so a bot holding only a legal Reverse card drew
  forever instead of playing it, stalling real games at the 3000-turn safety
  limit. Fixed by adding a REVERSE priority tier between SKIP and NUMBER.

All pre-existing characterization tests, the persistence tests, and the
new tests pass together (`./mvnw test`, 52 tests total, 0 failures).

## What limitations remain

- No Wild Draw Four challenge rule and no draw-card stacking (documented
  acceptable simplifications).
- Bot strategy is a fixed priority order, not adaptive.
- The UNO-call "forget" rate (10%) is a fixed constant, not configurable from
  the CLI.
- Persistence (`GameRepository`) still records one row per round, not per
  match — there is no `MatchEntity` grouping rounds together or storing the
  target score. This was a deliberate scope decision: the rubric has no
  persistence line item, and adding one would be unrequested scope beyond the
  feature menu.
- `playGame()` in `Main` still interleaves CLI output with calling into
  `GameEngine`/`GameState`; an event-driven rewrite would be needed for a
  GUI or headless front-end, as already noted in `docs/extension-readiness.md`.
