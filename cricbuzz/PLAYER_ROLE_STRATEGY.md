# Player Role Strategy Pattern

## Overview

The Player Role Strategy Pattern allows different player roles (Batter, Bowler, All-Rounder, etc.) to behave differently based on match conditions (T20, ODI, Test). This provides flexible, role-specific behavior without modifying the base Player class.

## Problem Solved

- **Different Requirements**: Each role has different minimum/maximum requirements based on match format
- **Flexible Behavior**: Roles behave differently in T20 vs ODI vs Test matches
- **Squad Selection**: Need intelligent squad selection based on role priorities
- **Match Conditions**: Same role may have different value/contribution in different formats

## Strategy Pattern Implementation

### Component Structure

```
PlayerRoleStrategy (Interface)
├── BatterStrategy
├── BowlerStrategy
├── AllRounderStrategy
├── WicketKeeperStrategy
└── BatterWicketKeeperStrategy
```

### Key Methods

Each strategy implements:
- `getMinimumRequired(MatchFormat)` - Minimum players needed
- `getMaximumAllowed(MatchFormat)` - Maximum players allowed
- `canBeSelected(Player, Match, List<Player>)` - Selection eligibility
- `getSelectionPriority(MatchFormat)` - Priority for squad selection
- `calculateContribution(Player, MatchFormat)` - Value calculation
- `getRoleRequirements(MatchFormat)` - Role-specific requirements

## Role-Specific Behaviors

### Batter Strategy

**T20 Format:**
- Minimum: 5 batters
- Maximum: 8 batters
- Priority: 1 (High - batting heavy format)
- Preferred: Aggressive, power hitting

**ODI Format:**
- Minimum: 6 batters
- Maximum: 8 batters
- Priority: 2 (Medium-high)
- Preferred: Balanced batting

**Test Format:**
- Minimum: 6 batters
- Maximum: 7 batters
- Priority: 3 (Lower - bowling more important)
- Preferred: Defensive, technique, patience

### Bowler Strategy

**T20 Format:**
- Minimum: 4 bowlers
- Maximum: 6 bowlers
- Priority: 2 (Medium)
- Preferred: Economy, yorkers, death bowling

**ODI Format:**
- Minimum: 5 bowlers
- Maximum: 7 bowlers
- Priority: 2 (Medium-high)
- Preferred: Variations, control

**Test Format:**
- Minimum: 5 bowlers
- Maximum: 6 bowlers
- Priority: 1 (High - crucial in Tests)
- Preferred: Line & length, swing, endurance

### All-Rounder Strategy

**T20 Format:**
- Minimum: 1 all-rounder
- Maximum: 3 all-rounders
- Priority: 1 (Very High - flexibility is key)
- Value: 1.5x base contribution

**ODI Format:**
- Minimum: 2 all-rounders
- Maximum: 4 all-rounders
- Priority: 1 (Very High)
- Value: 1.5x base contribution

**Test Format:**
- Minimum: 1 all-rounder
- Maximum: 2 all-rounders
- Priority: 2 (Medium - specialists preferred)
- Value: 1.5x base contribution

### Wicket Keeper Strategy

**All Formats:**
- Minimum: 1 (Mandatory)
- Maximum: 1 (Only one allowed)
- Priority: 0 (Highest - must have)
- Value: 1.3x base contribution

## Usage Examples

### Squad Validation

```java
SquadSelectionService service = new SquadSelectionService(matchRepository, playerRepository);
SquadValidationResult result = service.validateSquad(matchId, playerIds);

if (!result.isValid()) {
    result.getErrors().forEach(System.out::println);
}
```

### Squad Suggestion

```java
SquadSuggestion suggestion = service.suggestSquadComposition(MatchFormat.T20);
// Get requirements for each role
suggestion.getRequirements().forEach((role, req) -> {
    System.out.println(role + ": " + req.getMinimum() + "-" + req.getMaximum());
});
```

### Optimal Squad Selection

```java
List<Player> availablePlayers = // ... get from team
List<Player> optimalSquad = service.selectOptimalSquad(availablePlayers, match);
// Returns best 15 players based on role strategies
```

## Benefits

1. **Open/Closed Principle**: Add new roles without modifying existing code
2. **Single Responsibility**: Each strategy handles one role's behavior
3. **Flexibility**: Easy to adjust role behavior for different formats
4. **Testability**: Each strategy can be tested independently
5. **Maintainability**: Role logic is centralized in strategies

## Factory Pattern Integration

`PlayerRoleStrategyFactory` creates appropriate strategy based on `PlayerRole` enum:

```java
PlayerRoleStrategy strategy = PlayerRoleStrategyFactory.createStrategy(PlayerRole.BATTER);
```

## Integration with Squad Selection

The `SquadSelectionService` uses strategies to:
- Validate squad composition
- Suggest optimal squad structure
- Select best players based on role priorities
- Calculate player contributions

## Future Extensions

Easy to extend with:
- **Pitch Condition Strategies**: Different behavior on spin-friendly vs pace-friendly pitches
- **Weather Strategies**: Rain-affected match behavior
- **Opponent Strategies**: Role behavior based on opponent strength
- **Player Form Strategies**: Dynamic contribution based on recent form

