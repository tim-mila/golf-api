-- Create handicap table with audit fields
CREATE TABLE handicap
(
    handicap_id    VARCHAR(37) PRIMARY KEY,
    created_at     TIMESTAMP        NOT NULL,
    golfer_id      VARCHAR(255)     NOT NULL,
    handicap_index DOUBLE PRECISION NOT NULL,
    rounds_used    INTEGER          NOT NULL,
    total_rounds   INTEGER          NOT NULL
);

-- Index for querying handicaps by user (most common query pattern)
CREATE INDEX idx_handicap_golfer_id ON handicap (golfer_id);

-- Index for querying by golfer and date (for date-range queries)
CREATE INDEX idx_handicap_golfer_id_created_at ON handicap (golfer_id, created_at DESC);
