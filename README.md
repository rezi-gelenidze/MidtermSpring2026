# UNO CLI

A command-line UNO card game in Java. Supports bot-only and interactive (human vs bots) play.

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

## Logging

Game events are logged to `logs/uno.log` via SLF4J + Logback. Logged events include game start, player turns, cards played, cards drawn, invalid input, and game end. Logging does not interfere with normal CLI output.

## Rules

See `docs/rules.html` for the implemented game rules.
