-- Create a table to define the types of statistics we'll track
CREATE TABLE audit (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT, 
    is_higher_better BOOLEAN DEFAULT TRUE,
    UNIQUE(name),
    UNIQUE(code)
);

-- Create indices for commonly queried columns
CREATE INDEX idx_statistic_type_code ON statistic_type(code);

-- Create the main team statistics table
CREATE TABLE team_statistic (
    id SERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL REFERENCES team(id),
    season_id BIGINT NOT NULL REFERENCES season(id),
    statistic_date DATE NOT NULL,
    statistic_type_id BIGINT NOT NULL REFERENCES statistic_type(id),
    numeric_value DECIMAL(12,4),
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure we don't have duplicate statistics for the same team/date/type
    UNIQUE(team_id, season_id, statistic_date, statistic_type_id)
);

-- Create indices for common query patterns
CREATE INDEX idx_team_statistic_team_date ON team_statistic(team_id, statistic_date);
CREATE INDEX idx_team_statistic_date ON team_statistic(statistic_date);
CREATE INDEX idx_team_statistic_type_date ON team_statistic(statistic_type_id, statistic_date);
CREATE INDEX idx_team_statistic_team_type ON team_statistic(team_id, statistic_type_id);

-- -- Insert some common statistic types
-- INSERT INTO statistic_type (name, code, description, data_type, is_cumulative) VALUES
-- ('Wins', 'WINS', 'Total number of wins', 'INTEGER', true),
-- ('Losses', 'LOSSES', 'Total number of losses', 'INTEGER', true),
-- ('Win Percentage', 'WIN_PCT', 'Winning percentage', 'DECIMAL', false),
-- ('Average Points For', 'AVG_PTS_FOR', 'Average points scored per game', 'DECIMAL', false),
-- ('Average Points Against', 'AVG_PTS_AGAINST', 'Average points allowed per game', 'DECIMAL', false),
-- ('Home Win Percentage', 'HOME_WIN_PCT', 'Winning percentage for home games', 'DECIMAL', false),
-- ('Away Win Percentage', 'AWAY_WIN_PCT', 'Winning percentage for away games', 'DECIMAL', false),
-- ('Points Per Game', 'PPG', 'Points scored per game', 'DECIMAL', false),
-- ('Field Goal Percentage', 'FG_PCT', 'Field goal percentage', 'DECIMAL', false),
-- ('Three Point Percentage', 'THREE_PCT', 'Three point percentage', 'DECIMAL', false);

