-- Create scorecard table with audit fields
CREATE TABLE scorecard (
    scorecard_id VARCHAR(36) PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),
    score_date DATE NOT NULL,
    course_name CHARACTER VARYING(255) NOT NULL,
    tee_name CHARACTER VARYING(255) NOT NULL,
    score INTEGER NOT NULL,
    rating DOUBLE PRECISION NOT NULL,
    slope DOUBLE PRECISION NOT NULL,
    scorecard_type CHARACTER VARYING(20) NOT NULL,
    differential DOUBLE PRECISION NOT NULL,
    index_established BOOLEAN NOT NULL
);

-- Index for querying scorecards by user (most common query pattern)
CREATE INDEX idx_scorecard_created_by ON scorecard(created_by);

-- Index for querying by user and date (for date-range queries)
CREATE INDEX idx_scorecard_created_by_score_date ON scorecard(created_by, score_date DESC);
