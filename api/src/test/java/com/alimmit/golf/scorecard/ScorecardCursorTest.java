package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.alimmit.golf.errors.InvalidCursorException;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ScorecardCursorTest {

  private static final LocalDate DATE = LocalDate.of(2025, 6, 15);
  private static final UUID ID = UUID.fromString("01952a3b-f4c2-7000-8000-000000000003");

  @Test
  void encode_thenDecode_roundTrips() {
    ScorecardCursor original = new ScorecardCursor(DATE, ID);

    String encoded = original.encode();
    ScorecardCursor decoded = ScorecardCursor.decode(encoded);

    assertThat(decoded.scoreDate()).isEqualTo(DATE);
    assertThat(decoded.scorecardId()).isEqualTo(ID);
  }

  @Test
  void encode_producesUrlSafeBase64() {
    String encoded = new ScorecardCursor(DATE, ID).encode();

    // URL-safe base64 must not contain +, /, or = padding
    assertThat(encoded).doesNotContain("+", "/", "=");
  }

  @Test
  void decode_invalidBase64_throwsInvalidCursorException() {
    assertThatThrownBy(() -> ScorecardCursor.decode("not!!!valid base64"))
        .isInstanceOf(InvalidCursorException.class);
  }

  @Test
  void decode_validBase64ButMissingDateField_throwsInvalidCursorException() {
    // base64url of {"id":"01952a3b-f4c2-7000-8000-000000000003"} — no "d" field
    String noDate =
        java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"id\":\"01952a3b-f4c2-7000-8000-000000000003\"}".getBytes());

    assertThatThrownBy(() -> ScorecardCursor.decode(noDate))
        .isInstanceOf(InvalidCursorException.class);
  }

  @Test
  void decode_validBase64ButMissingIdField_throwsInvalidCursorException() {
    // base64url of {"d":"2025-06-15"} — no "id" field
    String noId =
        java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"d\":\"2025-06-15\"}".getBytes());

    assertThatThrownBy(() -> ScorecardCursor.decode(noId))
        .isInstanceOf(InvalidCursorException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {"bad-date", "2025-13-01", "not-a-date"})
  void decode_invalidDateValue_throwsInvalidCursorException(String badDate) {
    String encoded =
        java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                ("{\"d\":\"" + badDate + "\",\"id\":\"01952a3b-f4c2-7000-8000-000000000003\"}")
                    .getBytes());

    assertThatThrownBy(() -> ScorecardCursor.decode(encoded))
        .isInstanceOf(InvalidCursorException.class);
  }

  @Test
  void decode_invalidUuidValue_throwsInvalidCursorException() {
    String encoded =
        java.util.Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"d\":\"2025-06-15\",\"id\":\"not-a-uuid\"}".getBytes());

    assertThatThrownBy(() -> ScorecardCursor.decode(encoded))
        .isInstanceOf(InvalidCursorException.class);
  }

  @Test
  void decode_emptyString_throwsInvalidCursorException() {
    assertThatThrownBy(() -> ScorecardCursor.decode("")).isInstanceOf(InvalidCursorException.class);
  }
}
