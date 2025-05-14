
CREATE TABLE audit (
    id SERIAL PRIMARY KEY,
    command VARCHAR(50) NOT NULL,
    result VARCHAR(100) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users(id)
);

