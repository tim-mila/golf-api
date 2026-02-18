CREATE TABLE tee
(
    tee_id           UUID PRIMARY KEY DEFAULT uuidv7(),
    course_id        UUID                   NOT NULL REFERENCES course(course_id),
    created_at       TIMESTAMP              NOT NULL,
    created_by       CHARACTER VARYING(255) NOT NULL,
    last_modified_at TIMESTAMP              NOT NULL,
    last_modified_by CHARACTER VARYING(255) NOT NULL,
    name             CHARACTER VARYING(100) NOT NULL,
    yardage          INTEGER                NOT NULL,
    slope            NUMERIC(4, 1)          NOT NULL,
    rating           NUMERIC(4, 1)          NOT NULL
);

CREATE UNIQUE INDEX idx_unique_tee_per_course ON tee (course_id, name);

CREATE TABLE tee_aud
(
    rev              INTEGER NOT NULL REFERENCES revinfo(rev),
    revtype          INTEGER NOT NULL,
    revend           INTEGER,
    tee_id           UUID,
    course_id        UUID,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),
    name             CHARACTER VARYING(100),
    yardage          INTEGER,
    slope            NUMERIC(4, 1),
    rating           NUMERIC(4, 1),
    PRIMARY KEY (rev, tee_id)
);
