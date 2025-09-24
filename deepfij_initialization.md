# Database Initialization for DeepFij

## Purpose

Currently on startup, the startup service initializes the admin user and loads some schedule data. 
We want to extend this to make it more explicit and configurable.
To that end, the startup service will still initialize the admin user as it is.
We will remove the deepfij.seasonsToLoad application.properties item.
We will add a new configuration item deepfij.initialization which will point to a configuration file.

## Configuration File

- **Location**: The configuration file should be placed in `src/main/resources/` or specified via absolute path
- **Format**: JSON format (with potential future support for YAML)
- **Property**: `deepfij.initialization=classpath:deepfij-init.json` or `deepfij.initialization=/path/to/config.json`
- **Fallback**: If not specified or file not found, use minimal default configuration (admin user only)
An example configuration is given below:

```json
{
  "schedule": {
    "loadTeams": "always",
    "minTeams": 300,
    "loadConferences": "check",
    "minConferences": 10,
    "seasons": [
      {
        "year": 2024,
        "loadGames": "check",
        "isCurrent": false
      },
      {
        "year": 2025,
        "loadGames": "check",
        "isCurrent": false
      },
      {
        "year": 2026,
        "loadGames": "check",
        "isCurrent": true
      }
    ]
  },
  "statistics": {
    "statisticsToLoad": [
      {
        "key": "WONLOST",
        "seasons": [
          2024,
          2025,
          2026
        ]
      },
      {
        "key": "POINTS",
        "seasons": [
          2024,
          2025
        ]
      }
    ]
  },
  "models": {
    "modelsToTrain": [
      {
        "name": "naive-linear-regression",
        "parameters": {
          "seasons": [2025]
        }
      }
    ]
  }
}
```

## Detailed breakdown of the configuration 

The configuration has three main sections that execute sequentially: **schedule**, **statistics**, and **models**.

### Schedule Configuration

The schedule section is divided into three parts: teams, conferences, and seasons.

#### Teams Loading (`loadTeams`)
- **Values**: `"always"`, `"check"`, `"never"`
- **`"check"`**: Load teams only if teams table has fewer than `minTeams` entries
- **`"always"`**: Always reload teams from ESPN (will update existing teams)
- **`"never"`**: Skip team loading entirely

#### Conferences Loading (`loadConferences`) 
- **Values**: `"always"`, `"check"`, `"never"`
- **`"check"`**: Load conferences if:
  - Conferences table has fewer than `minConferences` entries, OR
  - Teams were loaded/reloaded in this initialization cycle
- **`"always"`**: Always reload conferences from ESPN
- **`"never"`**: Skip conference loading entirely

#### Seasons/Games Loading (`loadGames`)
- **Values**: `"always"`, `"check"`, `"never"`
- **`"check"`**: Load games for a season if:
  - No games exist for that season, OR
  - Teams or conferences were reloaded in this initialization cycle
- **`"always"`**: Always reload all games for that season
- **`"never"`**: Skip game loading for that season

#### Current Season Handling (`isCurrent`)
- **Purpose**: Identifies the active season for ongoing updates
- **Behavior**: If a season is marked as `isCurrent`, refresh games from one week ago to one week ahead of current date
- **Constraint**: Only one season should be marked as current

### Statistics Configuration

#### Statistics Loading (`statisticsToLoad`)
- **Purpose**: Load calculated statistics for teams based on game results
- **Logic**: For each statistic key and season combination:
  - Check if statistics exist for that key/season pair
  - Load statistics if missing OR if games were reloaded for that season
- **Keys**: Must correspond to valid `StatisticType` codes in the database (e.g., "WONLOST", "POINTS")

### Models Configuration

#### Model Training (`modelsToTrain`)
- **Purpose**: Train ML models using loaded data
- **Requirements**: 
  - Model name must correspond to a valid model implementation class
  - Parameters must match the model's expected parameter structure
- **Dependency**: All required statistics must be loaded before model training begins

## Error Handling & Validation

- **Configuration Validation**: JSON schema validation on startup
- **Dependency Checking**: Verify all referenced seasons, statistic keys, and model names exist
- **Rollback Strategy**: If any step fails, log error but continue with remaining steps
- **Monitoring**: Log initialization progress and timing for each step

## Implementation Notes

### Configuration Loading
- Use `@ConfigurationProperties` with `@JsonProperty` annotations
- Support both classpath and file system paths for configuration file
- Implement default fallback configuration

### State Tracking
- Track what data was loaded/reloaded during initialization cycle
- Use boolean flags to determine downstream dependencies (e.g., if teams reloaded → reload conferences)

### Performance Considerations
- Large data loads should be batched and logged with progress indicators  
- Consider making statistics and model training asynchronous for faster startup
- Implement configurable timeouts for ESPN API calls

### Database Considerations
- Ensure proper transaction boundaries for each initialization step
- Consider using separate database connections for initialization vs. application traffic
- Add database indexes on frequently queried columns during initialization