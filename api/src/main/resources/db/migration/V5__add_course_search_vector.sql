ALTER TABLE course ADD COLUMN search_vector tsvector;

CREATE INDEX idx_course_search_vector ON course USING GIN(search_vector);

CREATE TRIGGER course_search_vector_update
  BEFORE INSERT OR UPDATE ON course
  FOR EACH ROW EXECUTE FUNCTION
    tsvector_update_trigger(search_vector, 'pg_catalog.english', club, course, city, state);

-- Populate existing rows
UPDATE course SET search_vector = to_tsvector('pg_catalog.english',
  coalesce(club,'') || ' ' || coalesce(course,'') || ' ' || coalesce(city,'') || ' ' || coalesce(state,''));
