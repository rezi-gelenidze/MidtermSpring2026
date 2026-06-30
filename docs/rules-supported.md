# Rules Supported

This document lists which rules from `Final_Project_UNO_rules_reference.md` are
implemented, and documents every variant or simplification used, per the final
project's deliverable requirements.

## Deck Composition — Implemented

Standard 108-card deck: four colors, one `0` and two of each `1`-`9` per color,
two `Skip`/`Reverse`/`Draw Two` per color, four `Wild`, four `Wild Draw Four`.
Built in `GameState.buildDeck()`; verified by `DeckCompositionTest`.

## Legal Play Validation — Implemented

A card is legal if it matches the active color, matches the top card's number,
matches the top card's action type, or is a Wild/Wild Draw Four. After a wild
is played, the chosen color becomes the active color. Implemented in
`Rules.isLegal`; verified by the `Rules.isLegal` tests in `CharacterizationTest`.

## Skip — Implemented

The next player loses their turn; play continues with the player after them.
`GameState.applyCardEffect`; verified by `testSkipAdvancesTwice`.

## Reverse — Implemented (with a documented 2-player simplification)

Turn direction flips for three or more players. **In a 2-player game, Reverse
is treated like Skip** (the same player who played it goes again) — this is
the simplification explicitly permitted by the rules reference. Implemented
in `GameState.applyCardEffect`; verified by `testReverseFlipsDirection`.

## Draw Two — Implemented

The next player draws two cards and loses their turn; play continues with the
following player. No stacking. `GameState.applyCardEffect`; verified by
`testDrawTwoForcesDraw`.

## Wild — Implemented

The player chooses the next active color, which is then used for legal-play
checks; the next player takes a normal turn. `GameEngine.playGame` /
`GameState.setCalledColor`.

## Wild Draw Four — Implemented (no challenge rule)

The player chooses the next color, the next player draws four cards and loses
their turn, and play continues with the following player. **No challenge
rule** — this is an explicitly acceptable simplification per the rules
reference. `GameState.applyCardEffect`; verified by `testWildDrawFourForcesDraw`.

## Draw/Pass Behavior — Implemented

**Chosen variant:** draw one card, then auto-play it immediately if it is
legal; otherwise the turn passes without playing it. A human player may also
type `DRAW` even while holding a legal card — this quirk from the midterm
implementation is preserved unchanged. Implemented in `GameEngine.playGame`;
verified by the existing `testBotAutoPlayDrawn` (legal-drawn-card path) and
the new `DrawPassTest` (illegal-drawn-card → pass path).

## UNO Call And Missed-UNO Penalty — Implemented

**Timing rule (this implementation's chosen variant):** a player must declare
UNO at the moment their hand reaches exactly one card, immediately after
playing the card that brought them there. A human is prompted in the CLI
("Call UNO? (Y/N)"); a bot calls automatically 90% of the time and "forgets"
10% of the time (seeded, so deterministic with `--seed`). Every subsequent
turn, the engine scans every player's hand; anyone still sitting at exactly
one card without a successful declaration draws a two-card penalty. The
declaration must be made again any time the hand size changes away from one
and later returns to one (e.g. after a forced draw).

Implemented in `GameState.resolveUnoDeclaration` / `checkMissedUnoPenalties` /
`chooseBotCallUno`, wired into `GameEngine.playGame`. Verified by `UnoCallTest`.

## Round Scoring And Multi-Round Target — Implemented

At the end of a round, the winner scores the point value of every card left
in every other hand (`GameState.tallyPoints`); this already accumulated
across rounds in the original CLI. New: a `--target N` flag turns the
fixed `--games` count into a match — rounds continue (capped by `--games` as
a safety limit) until any player's cumulative score reaches `N`, at which
point a final winner is declared (highest score; ties broken by lowest player
index). If `--target` is omitted, behavior is unchanged from before: exactly
`--games` rounds are played with no winner declared.

Implemented in `Match.reachedTarget` / `Match.determineWinner` (pure, no
console dependency) plus the round loop in `Main.main()`. Verified by
`MatchTest`. There is no official default target score required by this
project, so none is assumed — pass `--target 500` for a traditional match.

## Acceptable Simplifications In Use (summary)

- No Wild Draw Four challenge rule.
- No draw-card stacking.
- 2-player Reverse behaves like Skip.
- Bots use a fixed-priority strategy (Draw Two > Skip > Reverse > Number > Wild) and a
  90%/10% UNO-call/forget rate, not adaptive play.
- Text-only CLI interaction; all hands are visible in the terminal.
- No target score is assumed by default; the match only runs to a target when
  `--target` is explicitly passed.
- Persistence (game history/stats) is recorded per round, as before; there is
  no separate "match" database record — the rubric does not require this and
  it would be unrequested scope beyond the final project's feature menu.
