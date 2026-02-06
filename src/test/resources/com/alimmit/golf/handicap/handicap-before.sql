CREATE OR REPLACE FUNCTION insert_handicap(id UUID, rev INTEGER, revtype INTEGER, created TIMESTAMP, index DOUBLE PRECISION, rounds INTEGER)
RETURNS VOID AS $$
BEGIN
    INSERT INTO revinfo VALUES (rev, EXTRACT(EPOCH FROM created));
    INSERT INTO handicap_aud VALUES (rev, revtype, null, id, created, '123', index, rounds, rounds);
    INSERT INTO handicap VALUES (id, created, created, '123', index, rounds, rounds)
        ON CONFLICT (handicap_id)
        DO UPDATE SET
           last_modified_at = created,
           handicap_index = index,
           rounds_used = rounds,
           total_rounds = rounds;
END;
$$ LANGUAGE plpgsql;

SELECT * FROM insert_handicap('019c33a5-4fcf-7972-838c-459db7cbe2f5', 1, 0, CURRENT_TIMESTAMP::TIMESTAMP - '3 days'::INTERVAL, 6.6, 2);
SELECT * FROM insert_handicap('019c33a5-4fcf-7972-838c-459db7cbe2f5', 2, 1, CURRENT_TIMESTAMP::TIMESTAMP - '2 days'::INTERVAL, 6.4, 3);
SELECT * FROM insert_handicap('019c33a5-4fcf-7972-838c-459db7cbe2f5', 3, 1, CURRENT_TIMESTAMP::TIMESTAMP - '1 days'::INTERVAL, 6.1, 4);
