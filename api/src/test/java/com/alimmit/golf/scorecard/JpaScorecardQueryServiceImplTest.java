package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JpaScorecardQueryServiceImplTest {

  @Mock private ScorecardRepository scorecardRepository;
  @Mock private ScorecardMapper scorecardMapper;

  @InjectMocks private JpaScorecardQueryServiceImpl service;

  private ScorecardEntity entity() {
    return new ScorecardEntity(
        LocalDate.of(2025, 9, 21),
        "Test Course",
        "blue",
        88,
        72,
        72.1,
        125.0,
        ScorecardType.EIGHTEEN,
        14.4,
        true);
  }

  private ScorecardDto dto(UUID id) {
    return new ScorecardDto(
        id,
        Instant.now(),
        "123",
        LocalDate.of(2025, 9, 21),
        "Test Course",
        "blue",
        88,
        72,
        72.1,
        125.0,
        ScorecardType.EIGHTEEN,
        14.4,
        true);
  }

  @Test
  void findAllForUser_returnsMappedDtos() {
    String userId = "user-123";
    ScorecardEntity entity = entity();
    ScorecardDto expected = dto(UUID.randomUUID());

    when(scorecardRepository.findAllCreatedBy(userId)).thenReturn(List.of(entity));
    when(scorecardMapper.toDto(entity)).thenReturn(expected);

    assertThat(service.findAllForUser(userId)).containsExactly(expected);
  }

  @Test
  void findAllForUser_noResults_returnsEmptyList() {
    String userId = "user-123";

    when(scorecardRepository.findAllCreatedBy(userId)).thenReturn(List.of());

    assertThat(service.findAllForUser(userId)).isEmpty();
  }
}
