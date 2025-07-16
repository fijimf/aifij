CREATE TABLE inspirational_quote (
    id BIGSERIAL PRIMARY KEY,
    quote TEXT NOT NULL,
    source VARCHAR(255) NOT NULL,
    tag VARCHAR(100)
);

CREATE INDEX idx_inspirational_quote_tag ON inspirational_quote (tag);