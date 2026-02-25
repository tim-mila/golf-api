CREATE TABLE course
(
    course_id        UUID PRIMARY KEY DEFAULT uuidv7(),
    created_at       TIMESTAMP              NOT NULL,
    created_by       CHARACTER VARYING(255) NOT NULL,
    last_modified_at TIMESTAMP              NOT NULL,
    last_modified_by CHARACTER VARYING(255) NOT NULL,
    club             CHARACTER VARYING(100) NOT NULL,
    course           CHARACTER VARYING(100) NOT NULL,
    city             CHARACTER VARYING(100) NOT NULL,
    state            CHARACTER VARYING(14) NOT NULL
);

-- There should not be multiple clubs/courses with the same name in the same city/state for the same user.
CREATE UNIQUE INDEX idx_unique_club_course_location ON course (created_by, club, course, city, state);

CREATE INDEX idx_course_created_by ON course (created_by);

CREATE TABLE course_aud
(
    rev              INTEGER NOT NULL REFERENCES revinfo(rev),
    revtype          INTEGER NOT NULL,
    revend           INTEGER,
    course_id        UUID,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),
    club             CHARACTER VARYING(100),
    course           CHARACTER VARYING(100),
    city             CHARACTER VARYING(100),
    state            CHARACTER VARYING(14),
    PRIMARY KEY (rev, course_id)
);

