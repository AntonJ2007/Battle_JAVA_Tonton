# Game Character Manager & Battle Arena

A Java Swing application for creating and managing game characters, then pitting them against each other in a turn-based arena. Built on top of the original `java-game-character` project.

## What's new

- **Four character types** with different behaviour:
  | Type | Attack | Special | Notes |
  |---|---|---|---|
  | Warrior | Triple Kick – 15 dmg | Way of the Dragon – 30 dmg | Steady melee |
  | Mage | Chrono Dash – 20 dmg | Zaman Force – 40 dmg | High damage |
  | Archer | Piercing Shot – 18 dmg, 20% crit (x2) | Arrow Rain – 3 random arrows (10–14 each) | Randomised |
  | Healer | Holy Smite – 12 dmg | Divine Light – 15 dmg + heals self 30 | Sustain |
- **Roster tab**: add, view, search (live filter by name), update and remove characters.
- **Arena tab**: turn-based battle with Attack, Special (2-turn cooldown) and **Undo**.
- **Custom dark theme**: gradient header, rounded buttons, colour-coded types, painted portraits (unique icon per type), animated health bars, and a shake animation when a character is hit.
- **Input validation**: name must be non-empty, ≤ 15 characters, letters/numbers/spaces only, and unique (case-insensitive); health must be a whole number from 1 to 500; update/remove require a selection; battles require two *different* fighters.

## How the course concepts are applied

1. **Abstraction** – `GameCharacter` is an abstract class. It declares abstract methods (`attack`, `specialMove`, `getType`, `getAttackName`, `getSpecialName`, `getDescription`, `getColor`) that `Warrior`, `Mage`, `Archer` and `Healer` each implement. Shared logic (health, cooldown, `takeDamage`, `heal`) lives in the base class, and `performSpecial` is a `final` template method that wraps each subclass's `specialMove` with cooldown handling. The GUI works with `GameCharacter` references, so polymorphism picks the right behaviour.
2. **Abstract Data Types**
   - `List<GameCharacter>` (`ArrayList`) – the roster, plus a filtered list for search results.
   - `Queue<GameCharacter>` (`ArrayDeque`) – turn order; `poll()` + `add()` rotates turns.
   - Stack via `Deque<Snapshot>` (`push`/`pop`) – undo history of battle states.
3. **GUI** – Java Swing: `JFrame`, `JTabbedPane`, `JTable`, `JComboBox`, `JSpinner`, custom-painted components (`RoundButton`, `HealthBar`, `Portrait`) using `Graphics2D` and `javax.swing.Timer` for animation.
4. **User input** – text field, combo box and spinner for creating/updating characters; table selection loads a character into the form; search box filters live; confirmation dialog before removal.

## How to run

Requires **JDK 11 or newer**.

```bash
# from the project's source root (the folder containing departmentalPRAC/)
javac departmentalPRAC/BATTLE2.java
java departmentalPRAC.BATTLE2
```

Or open the project in Eclipse/IntelliJ/VS Code and run `BATTLE2.main`.

## Using the app

1. **Roster tab** – fill in name, type and health, then *Add Character*. Click a row to edit it (*Update Selected*) or delete it (*Remove Selected*). Type in the search box to filter.
2. **Arena tab** – pick two different fighters, press *START BATTLE*, then take turns with *Attack* or *Special*. *Undo* steps back one move.
