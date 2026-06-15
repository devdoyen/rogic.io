CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    xp INTEGER NOT NULL,
    level INTEGER NOT NULL,
    uuid VARCHAR(255)
);

CREATE TABLE stages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    solution_grid TEXT,
    total_attempts INTEGER NOT NULL DEFAULT 0,
    total_clears INTEGER NOT NULL DEFAULT 0,
    average_elapsed_time DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    approved BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE histories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stage_id BIGINT NOT NULL,
    cleared_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    xp_earned INTEGER NOT NULL,
    elapsed_time INTEGER NOT NULL,
    CONSTRAINT fk_histories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_histories_stage FOREIGN KEY (stage_id) REFERENCES stages(id) ON DELETE CASCADE
);

CREATE TABLE visitor_logs (
    id BIGSERIAL PRIMARY KEY,
    uuid VARCHAR(255) NOT NULL,
    ip_hash VARCHAR(255) NOT NULL,
    visited_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    visited_date DATE NOT NULL
);
