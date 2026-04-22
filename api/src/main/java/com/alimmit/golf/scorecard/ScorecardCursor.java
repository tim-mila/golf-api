package com.alimmit.golf.scorecard;

import com.alimmit.golf.errors.InvalidCursorException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Opaque pagination cursor encoding the keyset position {@code (scoreDate, scorecardId)} used by
 * {@code GET /v1/scorecard}. Serialized as a base64url-encoded JSON string for clients; decoded
 * back to its component fields for the repository keyset query.
 */
record ScorecardCursor(LocalDate scoreDate, UUID scorecardId) {

  private static final Pattern DATE_PATTERN = Pattern.compile("\"d\":\"([^\"]+)\"");
  private static final Pattern ID_PATTERN = Pattern.compile("\"id\":\"([^\"]+)\"");

  /**
   * Decode an opaque cursor string produced by {@link #encode()}.
   *
   * @throws InvalidCursorException if the string is not a valid cursor
   */
  static ScorecardCursor decode(String cursor) {
    try {
      byte[] bytes = Base64.getUrlDecoder().decode(cursor);
      String json = new String(bytes, StandardCharsets.UTF_8);
      Matcher dateMatcher = DATE_PATTERN.matcher(json);
      Matcher idMatcher = ID_PATTERN.matcher(json);
      if (!dateMatcher.find() || !idMatcher.find()) {
        throw new InvalidCursorException();
      }
      return new ScorecardCursor(
          LocalDate.parse(dateMatcher.group(1)), UUID.fromString(idMatcher.group(1)));
    } catch (InvalidCursorException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidCursorException();
    }
  }

  /** Encode this cursor as an opaque base64url string safe to include in a query parameter. */
  String encode() {
    String json = "{\"d\":\"" + scoreDate + "\",\"id\":\"" + scorecardId + "\"}";
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(json.getBytes(StandardCharsets.UTF_8));
  }
}
