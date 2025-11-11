# Game Detail Endpoint - Clarified Specification

## Endpoint Definition

**Path**: `/api/game/{gameId}`
**Method**: `GET`
**Authentication**: Public (no authentication required)
**Response Type**: `ResponseEntity<ApiResponse<GameDetail>>`

## Core Principle: Point-in-Time Data

All statistics, records, and rankings represent the state of knowledge **immediately before** the specified game began. This means:
- **For completed games**: Include all games with dates strictly before this game's date
- **For future games**: Include all completed games up to "today"
- **Date comparison**: Use LocalDate comparison (ignore time-of-day)
- **Same-day games**: Exclude all games on the same date as the game in question

**Example**: For a game on 2024-12-15:
- Include: All games dated 2024-12-14 and earlier
- Exclude: Games dated 2024-12-15 and later

## Response Structure

### GameDetail (Root DTO)
```java
public record GameDetail(
    GameInfo game,
    TeamDetail homeTeam,
    TeamDetail awayTeam
) {}
```

### GameInfo
```java
public record GameInfo(
    Long id,
    LocalDate date,
    ConferenceInfo conference,              // nullable
    HeadToHeadRecord headToHead,
    List<PreviousMeeting> lastFiveMeetings,
    BettingLines bettingLines,              // nullable
    PredictedValues predictions
) {}
```

### TeamDetail
```java
public record TeamDetail(
    Long id,
    String name,
    String abbreviation,
    String logoUrl,                         // nullable
    ConferenceInfo conference,              // nullable
    Integer score,                          // nullable (null if game not complete)
    WinLossRecord overallRecord,
    WinLossRecord conferenceRecord,         // nullable (if not in conference)
    WinLossRecord homeRecord,
    WinLossRecord awayRecord,
    WinLossRecord neutralRecord,
    WinLossRecord lastFiveRecord,
    Streak currentStreak,
    TeamStatistics statistics,
    List<TeamGameResult> seasonGames
) {}
```

## Detailed Field Specifications

### 1. Game-Level Data

#### 1.1 Conference (nullable)
**Populated when**: Both teams are members of the same conference for this season
**Null when**: Teams from different conferences, or either team is independent
```java
public record ConferenceInfo(
    Long id,
    String name,
    String logoUrl                          // nullable
) {}
```

#### 1.2 Head-to-Head Record
**Scope**: Current season only (same season as the game in question)
**Temporal constraint**: Only games before the current game's date
```java
public record HeadToHeadRecord(
    int homeTeamWins,                       // 0 if no prior meetings this season
    int awayTeamWins                        // 0 if no prior meetings this season
) {}
```

#### 1.3 Last Five Meetings
**Scope**: Up to last 5 games between these two teams (may span multiple seasons)
**Ordering**: Most recent first (descending by date)
**Search limit**: Look back maximum 10 years
**May contain**: 0-5 games
```java
public record PreviousMeeting(
    LocalDate date,
    String homeTeamName,
    String homeTeamAbbreviation,
    int homeTeamScore,
    String awayTeamName,
    String awayTeamAbbreviation,
    int awayTeamScore
) {}
```

#### 1.4 Betting Lines (nullable - entire object)
**Source**: Database fields in Game entity (if available)
**Null when**: Betting data not available for this game
**All fields nullable**: Individual betting lines may be missing
```java
public record BettingLines(
    Double pointSpread,                     // nullable, positive = home team favored
    Double overUnder,                       // nullable, total points
    Integer homeMoneyLine,                  // nullable, American odds format
    Integer awayMoneyLine                   // nullable, American odds format
) {}
```

#### 1.5 Predicted Values (STUBS - to be implemented later)
**Current implementation**: All methods return null
**Future**: Will calculate based on team statistics and ML models
```java
public record PredictedValues(
    Double predictedSpread,                 // STUB: return null for now
    Double predictedOverUnder,              // STUB: return null for now
    Double homeWinProbability,              // STUB: return null for now (0.0-1.0 range)
    Double awayWinProbability,              // STUB: return null for now (0.0-1.0 range)
    Integer predictedHomeMoneyLine,         // STUB: return null for now
    Integer predictedAwayMoneyLine          // STUB: return null for now
) {}
```

### 2. Team-Level Data (Both Home and Away)

#### 2.1 Basic Info
- **id**: Team.id (Long)
- **name**: Team.name (String)
- **abbreviation**: Team.abbreviation (String)
- **logoUrl**: Team.logoUrl (String, nullable)
- **score**: Game score if complete, null otherwise (Integer, nullable)

#### 2.2 Conference
**Same as game-level ConferenceInfo**
**Populated from**: ConferenceMapping for this team and season
**Nullable**: If team is independent or conference mapping not found

#### 2.3 Win-Loss Records
**All records**: Count only games in the current season, before the game date
```java
public record WinLossRecord(
    int wins,
    int losses
) {}
```

Record types:
- **overallRecord**: All games
- **conferenceRecord**: Only conference games (nullable if not in conference)
- **homeRecord**: Only home games (neutral site not included)
- **awayRecord**: Only away games (neutral site not included)
- **neutralRecord**: Only neutral site games
- **lastFiveRecord**: Most recent 5 completed games (may be less than 5 early in season)

#### 2.4 Current Streak
**Definition**: Consecutive wins or losses in most recent games (current season only)  This data can be inferred from the statistics WIN_STREAK and LOSS_STREAK.
```java
public record Streak(
    StreakType type,                        // WIN or LOSS
    int count                               // number of consecutive games
) {}

public enum StreakType {
    WIN, LOSS
}
```
**Examples**:
- `{type: WIN, count: 3}` = Won last 3 games
- `{type: LOSS, count: 1}` = Lost last game
- If no games played yet: `{type: WIN, count: 0}`

#### 2.5 Team Statistics
**Source**: StatisticType and TeamStatistic tables
**Temporal constraint**: Statistics calculated as of the day before game date
**Nullable fields**: If statistic not yet calculated (early season or missing data)
```java
public record TeamStatistics(
    StatisticValue pointsForAvg,            // model_key: POINTS_FOR_AVG
    StatisticValue pointsForStdDev,         // model_key: POINTS_FOR_SD
    StatisticValue pointsAgainstAvg,        // model_key: POINTS_AGAINST_AVG
    StatisticValue pointsAgainstStdDev,     // model_key: POINTS_AGAINST_SD
    StatisticValue pointsCovariance,        // model_key: PTS_FOR_PTS_AGAINST_COV
    RankedStatistic linearRegression,       // model_key: LINEAR_REGRESSION
    RankedStatistic logisticRegression,     // model_key: LOGISTIC_REGRESSION
    RankedStatistic rpi                     // model_key: RPI (or calculate if not available)
) {}

public record StatisticValue(
    Double value                            // nullable
) {}

public record RankedStatistic(
    Double value,                           // nullable
    Integer rank                            // nullable, 1 = best
) {}
```

**Rank Calculation**:
- Query all teams' values for that statistic in that season.
- Sort descending (higher value = better rank)
- Assign rank 1 to highest value
- If value is null, rank is null

#### 2.6 Season Games
**Scope**: All completed games for this team in the current season
**Ordering**: Descending by date (most recent first)
**Include**: All games up to but NOT including the current game
**Perspective**: Always from this team's perspective
```java
public record TeamGameResult(
    LocalDate date,
    Long opponentId,
    String opponentName,
    String opponentAbbreviation,
    int teamScore,                          // this team's score
    int opponentScore,                      // opponent's score
    boolean isWin                           // convenience field
) {}
```

## Error Handling

### HTTP Status Codes
- **200 OK**: Game found, data returned successfully
- **400 Bad Request**: Invalid gameId format (not a number)
- **404 Not Found**: Game with specified ID does not exist
- **500 Internal Server Error**: Unexpected error during processing

### Error Response Format
Use standard `ApiResponse<T>` error format:
```json
{
  "success": false,
  "message": "Game not found with id: 12345",
  "data": null,
  "timestamp": "2024-12-15T10:30:00"
}
```

## Example JSON Response

```json
{
  "success": true,
  "message": "Game details retrieved successfully",
  "data": {
    "game": {
      "id": 12345,
      "date": "2024-12-15",
      "conference": {
        "id": 5,
        "name": "Big Ten",
        "logoUrl": "https://example.com/logos/bigten.png"
      },
      "headToHead": {
        "homeTeamWins": 1,
        "awayTeamWins": 0
      },
      "lastFiveMeetings": [
        {
          "date": "2024-02-10",
          "homeTeamName": "Michigan",
          "homeTeamAbbreviation": "MICH",
          "homeTeamScore": 78,
          "awayTeamName": "Ohio State",
          "awayTeamAbbreviation": "OSU",
          "awayTeamScore": 72
        }
      ],
      "bettingLines": {
        "pointSpread": -3.5,
        "overUnder": 145.5,
        "homeMoneyLine": -150,
        "awayMoneyLine": 130
      },
      "predictions": {
        "predictedSpread": null,
        "predictedOverUnder": null,
        "homeWinProbability": null,
        "awayWinProbability": null,
        "predictedHomeMoneyLine": null,
        "predictedAwayMoneyLine": null
      }
    },
    "homeTeam": {
      "id": 123,
      "name": "Michigan Wolverines",
      "abbreviation": "MICH",
      "logoUrl": "https://example.com/logos/michigan.png",
      "conference": {
        "id": 5,
        "name": "Big Ten",
        "logoUrl": "https://example.com/logos/bigten.png"
      },
      "score": 85,
      "overallRecord": {
        "wins": 10,
        "losses": 2
      },
      "conferenceRecord": {
        "wins": 5,
        "losses": 1
      },
      "homeRecord": {
        "wins": 6,
        "losses": 0
      },
      "awayRecord": {
        "wins": 3,
        "losses": 2
      },
      "neutralRecord": {
        "wins": 1,
        "losses": 0
      },
      "lastFiveRecord": {
        "wins": 4,
        "losses": 1
      },
      "currentStreak": {
        "type": "WIN",
        "count": 3
      },
      "statistics": {
        "pointsForAvg": {
          "value": 78.5
        },
        "pointsForStdDev": {
          "value": 8.2
        },
        "pointsAgainstAvg": {
          "value": 68.3
        },
        "pointsAgainstStdDev": {
          "value": 7.1
        },
        "pointsCovariance": {
          "value": 0.23
        },
        "linearRegression": {
          "value": 12.5,
          "rank": 8
        },
        "logisticRegression": {
          "value": 0.78,
          "rank": 5
        },
        "rpi": {
          "value": 0.6234,
          "rank": 12
        }
      },
      "seasonGames": [
        {
          "date": "2024-12-10",
          "opponentId": 456,
          "opponentName": "Penn State",
          "opponentAbbreviation": "PSU",
          "teamScore": 82,
          "opponentScore": 75,
          "isWin": true
        },
        {
          "date": "2024-12-08",
          "opponentId": 789,
          "opponentName": "Wisconsin",
          "opponentAbbreviation": "WISC",
          "teamScore": 71,
          "opponentScore": 74,
          "isWin": false
        }
      ]
    },
    "awayTeam": {
      "id": 456,
      "name": "Ohio State Buckeyes",
      "abbreviation": "OSU",
      "logoUrl": "https://example.com/logos/ohiostate.png",
      "conference": {
        "id": 5,
        "name": "Big Ten",
        "logoUrl": "https://example.com/logos/bigten.png"
      },
      "score": 82,
      "overallRecord": {
        "wins": 9,
        "losses": 3
      },
      "conferenceRecord": {
        "wins": 4,
        "losses": 2
      },
      "homeRecord": {
        "wins": 5,
        "losses": 1
      },
      "awayRecord": {
        "wins": 3,
        "losses": 2
      },
      "neutralRecord": {
        "wins": 1,
        "losses": 0
      },
      "lastFiveRecord": {
        "wins": 3,
        "losses": 2
      },
      "currentStreak": {
        "type": "WIN",
        "count": 2
      },
      "statistics": {
        "pointsForAvg": {
          "value": 75.2
        },
        "pointsForStdDev": {
          "value": 9.1
        },
        "pointsAgainstAvg": {
          "value": 70.8
        },
        "pointsAgainstStdDev": {
          "value": 8.3
        },
        "pointsCovariance": {
          "value": 0.18
        },
        "linearRegression": {
          "value": 10.2,
          "rank": 15
        },
        "logisticRegression": {
          "value": 0.72,
          "rank": 12
        },
        "rpi": {
          "value": 0.5987,
          "rank": 18
        }
      },
      "seasonGames": [
        {
          "date": "2024-12-12",
          "opponentId": 321,
          "opponentName": "Indiana",
          "opponentAbbreviation": "IND",
          "teamScore": 88,
          "opponentScore": 79,
          "isWin": true
        }
      ]
    }
  },
  "timestamp": "2024-12-15T10:30:00"
}
```

## Implementation Guidelines

### Service Layer
Create a new `GameDetailService` class with the following structure:

```java
@Service
public class GameDetailService {

    // Main public method
    public GameDetail getGameDetail(Long gameId);

    // Private utility methods for each major component
    private GameInfo buildGameInfo(Game game);
    private TeamDetail buildTeamDetail(Team team, Game game, boolean isHomeTeam);

    // Game-level utilities
    private ConferenceInfo getConferenceForGame(Game game);
    private HeadToHeadRecord calculateHeadToHead(Game game);
    private List<PreviousMeeting> getLastFiveMeetings(Team homeTeam, Team awayTeam, LocalDate beforeDate);
    private BettingLines extractBettingLines(Game game);
    private PredictedValues calculatePredictions(Game game);  // STUB

    // Team-level utilities
    private WinLossRecord calculateOverallRecord(Team team, Season season, LocalDate beforeDate);
    private WinLossRecord calculateConferenceRecord(Team team, Season season, LocalDate beforeDate);
    private WinLossRecord calculateHomeRecord(Team team, Season season, LocalDate beforeDate);
    private WinLossRecord calculateAwayRecord(Team team, Season season, LocalDate beforeDate);
    private WinLossRecord calculateNeutralRecord(Team team, Season season, LocalDate beforeDate);
    private WinLossRecord calculateLastFiveRecord(Team team, Season season, LocalDate beforeDate);
    private Streak calculateCurrentStreak(Team team, Season season, LocalDate beforeDate);
    private TeamStatistics getTeamStatistics(Team team, Season season, LocalDate beforeDate);
    private List<TeamGameResult> getSeasonGames(Team team, Season season, LocalDate beforeDate);

    // Statistics utilities
    private StatisticValue getStatisticValue(Team team, Season season, String modelKey, LocalDate beforeDate);
    private RankedStatistic getRankedStatistic(Team team, Season season, String modelKey, LocalDate beforeDate);
    private Integer calculateRank(Team team, Season season, String modelKey, LocalDate beforeDate);
}
```

### Controller Layer
Add method to `ScheduleController` (or create new `GameController`):

```java
@GetMapping("/game/{gameId}")
public ResponseEntity<ApiResponse<GameDetail>> getGameDetail(@PathVariable Long gameId) {
    try {
        GameDetail gameDetail = gameDetailService.getGameDetail(gameId);
        return ResponseEntity.ok(ApiResponse.success(gameDetail, "Game details retrieved successfully"));
    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Game not found with id: " + gameId));
    } catch (Exception e) {
        logger.error("Error retrieving game details", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Error retrieving game details"));
    }
}
```

### Performance Considerations
1. **Use @EntityGraph** to fetch Game with Teams, Season, and Conferences in one query
2. **Cache statistics** lookups (same season + date will be queried multiple times)
3. **Batch queries** for game lists instead of N+1 queries
4. **Consider @Cacheable** for frequently accessed games (especially historical games that won't change)

### Testing Strategy
1. **Unit tests** for each utility method
2. **Integration test** for complete endpoint with sample data
3. **Edge cases**:
   - Game with no prior meetings
   - Game early in season (minimal statistics)
   - Future game
   - Teams from different conferences
   - Independent teams
   - Game with missing betting data
   - Teams with no games yet this season

## Database Fields Mapping

### Game Entity Fields Needed
- Verify presence of betting line fields:
  - `pointSpread` (Double)
  - `overUnder` (Double)
  - `homeMoneyLine` (Integer)
  - `awayMoneyLine` (Integer)
- If missing, add via Flyway migration

### Repository Queries Needed
Create custom queries in repositories:
1. `GameRepository.findGamesBetweenTeams(Team t1, Team t2, LocalDate beforeDate, Pageable)`
2. `GameRepository.findByTeamAndSeasonBeforeDate(Team team, Season season, LocalDate beforeDate)`
3. `TeamStatisticRepository.findByTeamAndSeasonAndModelKeyAndDateBefore(...)`
4. `TeamStatisticRepository.findAllBySeasonAndModelKeyAndDateBefore(...)` (for ranking)

## Future Enhancements (Not in Initial Implementation)
1. Implement prediction stub methods with ML models
2. Add RPI calculation if not already available as statistic
3. Cache frequently accessed game details
4. Add pagination for season games if list becomes very large
5. Websocket updates for in-progress games
6. Additional betting metrics (implied probabilities, value calculations)

## Questions Resolved
All ambiguities from the original specification have been resolved with specific decisions. If any business requirements differ from decisions made here, please update this specification before implementation.
