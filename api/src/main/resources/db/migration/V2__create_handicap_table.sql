
-- Create handicap table with audit fields
CREATE TABLE handicap
(
    handicap_id      UUID PRIMARY KEY DEFAULT uuidv7(),
    created_at       TIMESTAMP        NOT NULL,
    last_modified_at TIMESTAMP        NOT NULL,
    golfer_id        VARCHAR(255)     NOT NULL,
    handicap_index   DOUBLE PRECISION NOT NULL,
    rounds_used      INTEGER          NOT NULL,
    total_rounds     INTEGER          NOT NULL
);

-- Unique index for querying handicaps by user (most common query pattern)
CREATE UNIQUE INDEX idx_handicap_golfer_id ON handicap (golfer_id);

-- Index for querying by golfer and date (for date-range queries)
CREATE INDEX idx_handicap_golfer_id_created_at ON handicap (golfer_id, created_at DESC);

CREATE SEQUENCE revinfo_seq INCREMENT BY 50 START WITH 1;

CREATE TABLE revinfo
(
    rev      BIGINT PRIMARY KEY,
    revtstmp BIGINT
);


CREATE TABLE handicap_aud
(
    rev              INTEGER          NOT NULL REFERENCES revinfo(rev),
    revtype          INTEGER          NOT NULL,
    revend           INTEGER,
    handicap_id      UUID             NOT NULL,
    last_modified_at TIMESTAMP        NOT NULL,
    golfer_id        VARCHAR(255)     NOT NULL,
    handicap_index   DOUBLE PRECISION NOT NULL,
    rounds_used      INTEGER          NOT NULL,
    total_rounds     INTEGER          NOT NULL,
    PRIMARY KEY (rev, handicap_id)
);




