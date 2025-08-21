CREATE TABLE models
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(255) NOT NULL,
    description VARCHAR(1023),
    class_name  VARCHAR(511) NOT NULL,
    pipeline    BYTEA        NULL,
    features_ok BOOLEAN      NOT NULL,
    pipeline_ok BOOLEAN      NOT NULL,
    UNIQUE (name)
);

CREATE TABLE model_runs
(
    id         SERIAL PRIMARY KEY,
    model_id   BIGINT       NOT NULL REFERENCES models (id),
    run_date   TIMESTAMP    NOT NULL,
    run_status VARCHAR(255) NOT NULL,
    run_result BYTEA        NULL
);

CREATE TABLE model_run_params
(
    id           SERIAL PRIMARY KEY,
    model_run_id BIGINT       NOT NULL REFERENCES model_runs (id),
    param_name   VARCHAR(255) NOT NULL,
    param_value  VARCHAR(255) NOT NULL
);

CREATE TABLE model_run_metrics
(
    id           SERIAL PRIMARY KEY,
    model_run_id BIGINT NOT NULL REFERENCES model_runs (id),
    metric_name  VARCHAR(255),
    metric_value VARCHAR(255)
)