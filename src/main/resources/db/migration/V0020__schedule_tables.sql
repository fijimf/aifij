CREATE TABLE conference (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    short_name VARCHAR(50) NOT NULL,
    logo_url VARCHAR(255),
    espn_id VARCHAR(50),
    UNIQUE(name),
    UNIQUE(short_name)
);

CREATE TABLE team (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    abbreviation VARCHAR(5) NOT NULL,
    slug VARCHAR(150) NOT NULL,
    long_name VARCHAR(150) NOT NULL,
    espn_id VARCHAR(100) NOT NULL,
    primary_color VARCHAR(7),
    secondary_color VARCHAR(7),
    logo_url VARCHAR(255),
    UNIQUE(name),
    UNIQUE(abbreviation),
    UNIQUE(slug),
    UNIQUE(long_name),
    UNIQUE(espn_id)
);

CREATE TABLE season (
    id SERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    name VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    UNIQUE(year),
    UNIQUE(name)
);

CREATE TABLE conference_mapping (
    id SERIAL PRIMARY KEY,
    season_id BIGINT NOT NULL REFERENCES season(id),
    team_id BIGINT NOT NULL REFERENCES team(id),
    conference_id BIGINT NOT NULL REFERENCES conference(id),
    UNIQUE(season_id, team_id)
);

CREATE TABLE game (
    id SERIAL PRIMARY KEY,
    espn_id VARCHAR(20) NOT NULL,
    season_id BIGINT NOT NULL REFERENCES season(id),
    date DATE NOT NULL,
    time TIME,
    home_team_id BIGINT NOT NULL REFERENCES team(id),
    away_team_id BIGINT NOT NULL REFERENCES team(id),
    home_score INTEGER,
    away_score INTEGER,
    status VARCHAR(20),
    periods INTEGER,
    location VARCHAR(100),
    neutral_site BOOLEAN DEFAULT FALSE,
    home_team_seed INTEGER,
    away_team_seed INTEGER,
    spread FLOAT,
    over_under FLOAT,
    home_money_line INTEGER,
    away_money_line INTEGER

    CONSTRAINT game_teams_different CHECK (home_team_id != away_team_id)
);

CREATE INDEX idx_game_season ON game(season_id);
CREATE INDEX idx_game_date ON game(date);
CREATE INDEX idx_game_home_team ON game(home_team_id);
CREATE INDEX idx_game_away_team ON game(away_team_id);
CREATE INDEX idx_conference_mapping_season ON conference_mapping(season_id);
CREATE INDEX idx_conference_mapping_team ON conference_mapping(team_id);
CREATE INDEX idx_conference_mapping_conference ON conference_mapping(conference_id);
