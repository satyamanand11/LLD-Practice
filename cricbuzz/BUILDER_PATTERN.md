# Builder Pattern for Match and Tournament Construction

## Overview

The Builder Pattern is used for constructing complex Match and Tournament objects step-by-step. This allows flexible construction with validation and a fluent API.

## Problem Solved

- **Complex Construction**: Matches and Tournaments have many optional components
- **Validation**: Ensures all required fields are set before construction
- **Flexibility**: Allows setting optional fields in any order
- **Readability**: Fluent API makes construction code more readable
- **Immutability**: Can build immutable objects with all fields set at once

## Implementation

### MatchBuilder

Constructs Match objects with:
- **Required**: Tournament, Format, Teams, Venue
- **Optional**: Scheduled Time, Umpires, Scorers, Squads

### TournamentBuilder

Constructs Tournament objects with:
- **Required**: Name, Format
- **Optional**: Dates, Teams, Matches, Initial Status

## Usage Examples

### Building a Match

```java
Match match = new MatchBuilder()
    .setTournament("TOUR_12345")
    .setFormat(MatchFormat.ODI)
    .setTeams("TEAM_IND", "TEAM_AUS")
    .setVenue("Wankhede Stadium, Mumbai")
    .setScheduledTime(LocalDateTime.now().plusDays(1))
    .addUmpire("UMP_001")
    .addUmpire("UMP_002")
    .addScorer("SCR_001")
    .setSquad("TEAM_IND", Arrays.asList("PLAYER_1", "PLAYER_2", ...))
    .setSquad("TEAM_AUS", Arrays.asList("PLAYER_10", "PLAYER_11", ...))
    .build();
```

### Building a Tournament

```java
Tournament tournament = new TournamentBuilder()
    .setName("ICC World Cup 2024")
    .setFormat(MatchFormat.ODI)
    .setDates(LocalDate.now(), LocalDate.now().plusDays(30))
    .setInitialStatus(TournamentStatus.SCHEDULED)
    .addTeam("TEAM_IND")
    .addTeam("TEAM_AUS")
    .addTeam("TEAM_ENG")
    .addTeams(Arrays.asList("TEAM_NZ", "TEAM_SA"))
    .build();
```

## Benefits

1. **Validation**: All required fields validated before construction
2. **Flexibility**: Optional fields can be set in any order
3. **Readability**: Fluent API is self-documenting
4. **Error Prevention**: Compile-time safety for required fields
5. **Immutability**: Can create fully-formed objects

## Integration

The builders can be used:
- Directly in services
- Through the facade layer
- In command objects
- For testing and demos

## Validation

Builders validate:
- Required fields are not null/empty
- Team IDs are different (for matches)
- Dates are valid (end after start)
- No duplicate teams/matches/umpires

