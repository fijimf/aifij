# GameDetailService Timing Instrumentation

## Overview

Comprehensive timing logs have been added to `GameDetailService` to monitor performance and identify bottlenecks before optimization. All timing measurements are in milliseconds.

## Log Levels

- **INFO**: Overall request timing (start/end of `getGameDetail()`)
- **DEBUG**: Detailed method and database query timings

## Log Structure

The logs use indentation to show the call hierarchy:

```
INFO  - getGameDetail start/end (overall timing)
DEBUG -   Top-level methods (buildGameInfo, buildTeamDetail)
DEBUG -     Mid-level methods (calculateHeadToHead, getTeamStatistics)
DEBUG -       Database queries (DB: findByTeamAndSeasonBeforeDate)
DEBUG -         Statistic queries (DB: findByModelKey, etc.)
```

## Key Metrics Logged

### Overall Request
- `getGameDetail completed in X ms` - Total time for entire request

### Game Info Section
- `buildGameInfo completed in X ms`
  - `getConferenceForGame: X ms`
  - `calculateHeadToHead: X ms` (includes DB query)
  - `getLastFiveMeetings: X ms` (includes DB query)

### Team Detail Section (called twice: home & away)
- `buildTeamDetail (home/away) completed in X ms`
  - `getTeamConference: X ms` (includes DB query)
  - `calculateOverallRecord: X ms` (includes DB query)
  - `calculateConferenceRecord: X ms` (includes DB query + conference map build)
  - `calculateHomeRecord: X ms` (includes DB query)
  - `calculateAwayRecord: X ms` (includes DB query)
  - `calculateNeutralRecord: X ms` (includes DB query)
  - `calculateLastFiveRecord: X ms` (includes DB query)
  - `calculateCurrentStreak: X ms` (uses statistic queries)
  - `getTeamStatistics: X ms` (includes 8 statistic queries)
  - `getSeasonGames (X games): X ms` (includes DB query)

### Database Query Details

All DB queries log their execution time and result count:

**Game Queries:**
- `DB: findBetweenTeamsInSeason (X games): X ms`
- `DB: findBetweenTeams (X games): X ms`
- `DB: findByTeamAndSeasonBeforeDate (X games): X ms`

**Conference Queries:**
- `DB: findByTeamAndSeason: X ms`
- `DB: findBySeason (X mappings): X ms`

**Statistics Queries:**
- `DB: findByModelKey (model_key): X ms`
- `DB: findByTeamAndStatisticTypeAndSeason (model_key, X stats): X ms`
- `DB: findByStatisticTypeAndSeason (model_key, X stats): X ms`

### Statistics Processing

**Simple Statistics (no ranking):**
- `Simple statistics (5 values): X ms` - Time to fetch all 5 simple stats

**Ranked Statistics:**
- `LINEAR_REGRESSION (ranked): X ms` - Time for value + rank calculation
- `LOGISTIC_REGRESSION (ranked): X ms`
- `RPI (ranked): X ms`

Each ranked statistic includes:
- Database queries for statistic type and values
- Rank calculation processing time
- Final rank value

## Example Log Output

```log
INFO  - Fetching game detail for game ID: 12345
DEBUG - Game lookup completed in 5 ms
DEBUG - buildGameInfo completed in 127 ms
DEBUG -   getConferenceForGame: 3 ms
DEBUG -   calculateHeadToHead: 45 ms
DEBUG -     DB: findBetweenTeamsInSeason (2 games): 42 ms
DEBUG -   getLastFiveMeetings: 79 ms
DEBUG -     DB: findBetweenTeams (5 games): 76 ms
DEBUG - buildTeamDetail (home) completed in 892 ms
DEBUG -   getTeamConference (home): 8 ms
DEBUG -     DB: findByTeamAndSeason: 7 ms
DEBUG -   calculateOverallRecord (home): 34 ms
DEBUG -     DB: findByTeamAndSeasonBeforeDate (28 games): 32 ms
DEBUG -   calculateConferenceRecord (home): 67 ms
DEBUG -     DB: findByTeamAndSeasonBeforeDate (28 games): 31 ms
DEBUG -     buildConferenceMap (354 teams): 34 ms
DEBUG -       DB: findBySeason (354 mappings): 32 ms
DEBUG -   getTeamStatistics (home): 645 ms
DEBUG -     Simple statistics (5 values): 124 ms
DEBUG -       DB: findByModelKey (POINTS_FOR_AVG): 2 ms
DEBUG -       DB: findByTeamAndStatisticTypeAndSeason (POINTS_FOR_AVG, 30 stats): 23 ms
DEBUG -       ... (4 more simple stats)
DEBUG -     LINEAR_REGRESSION (ranked): 187 ms
DEBUG -       DB: findByModelKey (LINEAR_REGRESSION): 3 ms
DEBUG -       DB: findByTeamAndStatisticTypeAndSeason (LINEAR_REGRESSION, 30 stats): 28 ms
DEBUG -       DB: findByStatisticTypeAndSeason (LINEAR_REGRESSION, 10620 stats): 142 ms
DEBUG -       Rank processing (LINEAR_REGRESSION, 354 teams): 12 ms - RANK: 8
DEBUG -   getSeasonGames (home, 28 games): 36 ms
DEBUG -     DB: findByTeamAndSeasonBeforeDate (28 games): 34 ms
DEBUG - buildTeamDetail (away) completed in 881 ms
DEBUG -   ... (similar structure for away team)
INFO  - getGameDetail completed in 1923 ms for game ID: 12345
```

## Enabling Debug Logs

To see all timing details, configure your logging level in `application.properties` or `application-{profile}.properties`:

```properties
# Show all timing logs
logging.level.com.fijimf.deepfij.service.GameDetailService=DEBUG

# Or just show overall timing
logging.level.com.fijimf.deepfij.service.GameDetailService=INFO
```

## Performance Expectations

Based on typical data volumes:

**Fast Operations (< 50ms):**
- Game lookup
- Conference info retrieval
- Simple statistic queries

**Medium Operations (50-200ms):**
- Head-to-head calculations
- Win-loss record calculations
- Conference map building
- Simple statistics collection (5 values)

**Slow Operations (> 200ms):**
- Ranked statistic calculations (requires all team data)
- Full team detail building (multiple DB queries)
- Season games list generation (large result sets)

## Optimization Targets

When logs indicate performance issues, consider:

1. **N+1 Query Problems**: Multiple calls to `findByTeamAndSeasonBeforeDate` for same team
   - Solution: Cache or batch the game list retrieval

2. **Ranked Statistics**: Fetches all team data for ranking
   - Solution: Pre-calculate and store rankings
   - Alternative: Use database window functions for ranking

3. **Conference Map**: Fetches all mappings every time
   - Solution: Cache conference mappings per season

4. **Statistics Queries**: Multiple queries for 8 different statistic types
   - Solution: Batch fetch all needed statistics in one query
   - Alternative: Pre-join statistics in main query

## No Code Changes Required

All timing logs use existing SLF4J logger. No additional dependencies or configuration changes are needed. The logs have no performance impact when DEBUG level is disabled.
