# UNO CLI

A command-line UNO card game in Java. Supports bot-only and interactive (human vs bots) play with persistent game history.

## Prerequisites

- Java 11+
- Docker (for containerized builds)

## Local Build

```bash
./mvnw compile
```

## Local Test

```bash
./mvnw test
```

## Local Run

Bot-only game:

```bash
./mvnw exec:java -Dexec.args="--bots 3 --games 5 --quiet"
```

Interactive game:

```bash
./mvnw exec:java -Dexec.args="--human --bots 2 --games 1"
```

Multi-round match to a target score (rounds continue, accumulating scores,
until a player reaches the target or `--games` is hit as a safety cap; a
final match winner is then declared):

```bash
./mvnw exec:java -Dexec.args="--bots 3 --games 50 --target 500"
```

Without `--target`, `--games` behaves exactly as before: a fixed number of
independent rounds with no declared winner.

When a player's hand drops to one card, the CLI prompts `Call UNO? (Y/N):`.
Missing the call draws a two-card penalty before that player's next turn.

Card input examples:

```text
R5   red 5
YS   yellow skip
BR   blue reverse
G+2  green draw two
W    wild
W4   wild draw four
draw draw a card
```

## Package

```bash
./mvnw package
```

This creates `target/uno-cli-1.0-SNAPSHOT.jar`. Run it directly:

```bash
java -jar target/uno-cli-1.0-SNAPSHOT.jar --bots 3 --games 5 --quiet
```

## Docker Build

```bash
docker build -t uno-game .
```

## Docker Run

```bash
docker run uno-game --bots 3 --games 5 --quiet
```

Interactive game in Docker:

```bash
docker run -it uno-game --human --bots 2 --games 1
```

## Database

### Selected Database and ORM

- **Database**: H2 (embedded, file-based)
- **ORM**: Hibernate 5.6 (JPA provider)

### Schema

The database is auto-created on first run at `./data/uno.mv.db`. Schema is managed by Hibernate (`hbm2ddl.auto=update`).

Tables:

- `players` — unique player names (`id`, `name`)
- `games` — game records (`id`, `started_at`, `ended_at`, `rounds_played`, `winner_id`)
- `game_player_scores` — per-player scores for each game (`id`, `game_id`, `player_id`, `score`)

No manual schema setup is required.

### Persisted Data

After each completed game, the following is automatically saved:

- Player names (created once, reused across games)
- Game start and end timestamps
- Number of rounds played
- Per-player scores
- Winner

### Viewing Game History and Statistics

Show all reports:

```bash
./mvnw exec:java -Dexec.args="--stats"
```

Show recent games only:

```bash
./mvnw exec:java -Dexec.args="--recent"
```

Show player win counts:

```bash
./mvnw exec:java -Dexec.args="--wins"
```

Show highest scores:

```bash
./mvnw exec:java -Dexec.args="--top-scores"
```

### Persistence Tests

Persistence tests use an in-memory H2 database (`uno-test` persistence unit) and run automatically with:

```bash
./mvnw test
```

No external database setup is needed. Tests are fully isolated and do not depend on any local machine state.

## Logging

Game events are logged to `logs/uno.log` via SLF4J + Logback. Logged events include game start, player turns, cards played, cards drawn, invalid input, and game end. Logging does not interfere with normal CLI output.

## Rules

See `docs/rules-supported.md` for the final-project rule set and documented
simplifications, and `docs/rules.html` for the original midterm rule
reference. See `docs/final-report.md` for the final project report.
