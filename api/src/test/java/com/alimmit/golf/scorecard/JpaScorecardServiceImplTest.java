package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.alimmit.golf.differential.Differential;
import com.alimmit.golf.differential.DifferentialCalculator;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

@ExtendWith(MockitoExtension.class)
class JpaScorecardServiceImplTest {

  @Mock private ScorecardRepository scorecardRepository;
  @Mock private ScorecardMapper scorecardMapper;
  @Mock private DifferentialCalculator differentialCalculator;

  @InjectMocks private JpaScorecardServiceImpl service;

  private final ScorecardRequestDto request =
      new ScorecardRequestDto(
          LocalDate.of(2025, 9, 21),
          ScorecardType.EIGHTEEN,
          "Test Course",
          "blue",
          88,
          72,
          72.1,
          125.0);

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

  private ScorecardEntity entityWithId(UUID id) {
    ScorecardEntity e = entity();
    e.setId(id);
    return e;
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
  void create_calculatesAndPersistsScorecard() {
    Differential differential = new Differential(14.4, true);
    ScorecardEntity entity = entity();
    ScorecardDto expected = dto(UUID.randomUUID());

    when(differentialCalculator.calculateDifferential(request)).thenReturn(differential);
    when(scorecardMapper.toEntity(request, 14.4, true)).thenReturn(entity);
    when(scorecardRepository.save(entity)).thenReturn(entity);
    when(scorecardMapper.toDto(entity)).thenReturn(expected);

    ScorecardDto result = service.create(request);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void list_firstPage_returnsMappedDtos() {
    ScorecardEntity entity = entityWithId(UUID.randomUUID());
    ScorecardDto expected = dto(entity.getId());

    when(scorecardRepository.findFirstPageForCurrentUser(any(Limit.class)))
        .thenReturn(List.of(entity));
    when(scorecardMapper.toDto(entity)).thenReturn(expected);

    ScorecardPageDto result = service.list(20, null);

    assertThat(result.data()).containsExactly(expected);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
  }

  @Test
  void list_firstPage_hasNext_whenMoreResultsExist() {
    // Return limit+1 entities to trigger hasNext=true
    int limit = 2;
    ScorecardEntity e1 = entityWithId(UUID.randomUUID());
    ScorecardEntity e2 = entityWithId(UUID.randomUUID());
    ScorecardEntity e3 = entityWithId(UUID.randomUUID()); // the extra row
    ScorecardDto d1 = dto(e1.getId());
    ScorecardDto d2 = dto(e2.getId());

    when(scorecardRepository.findFirstPageForCurrentUser(any(Limit.class)))
        .thenReturn(List.of(e1, e2, e3));
    when(scorecardMapper.toDto(e1)).thenReturn(d1);
    when(scorecardMapper.toDto(e2)).thenReturn(d2);

    ScorecardPageDto result = service.list(limit, null);

    assertThat(result.data()).containsExactly(d1, d2);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.nextCursor()).isNotBlank();
  }

  @Test
  void list_withCursor_callsKeysetQuery() {
    ScorecardCursor cursor = new ScorecardCursor(LocalDate.of(2025, 6, 1), UUID.randomUUID());
    ScorecardEntity entity = entityWithId(UUID.randomUUID());
    ScorecardDto expected = dto(entity.getId());

    when(scorecardRepository.findNextPageForCurrentUser(
            any(LocalDate.class), any(UUID.class), any(Limit.class)))
        .thenReturn(List.of(entity));
    when(scorecardMapper.toDto(entity)).thenReturn(expected);

    ScorecardPageDto result = service.list(20, cursor);

    assertThat(result.data()).containsExactly(expected);
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  void list_emptyResults_returnsEmptyPage() {
    when(scorecardRepository.findFirstPageForCurrentUser(any(Limit.class))).thenReturn(List.of());

    ScorecardPageDto result = service.list(20, null);

    assertThat(result.data()).isEmpty();
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
  }

  @Test
  void getById_found_returnsDto() {
    UUID id = UUID.randomUUID();
    ScorecardEntity entity = entity();
    ScorecardDto expected = dto(id);

    when(scorecardRepository.findByIdForCurrentUser(id)).thenReturn(Optional.of(entity));
    when(scorecardMapper.toOptionalDto(entity)).thenReturn(Optional.of(expected));

    assertThat(service.getById(id)).contains(expected);
  }

  @Test
  void getById_notFound_returnsEmpty() {
    UUID id = UUID.randomUUID();

    when(scorecardRepository.findByIdForCurrentUser(id)).thenReturn(Optional.empty());

    assertThat(service.getById(id)).isEmpty();
  }

  @Test
  void deleteById_returnsRepositoryRowCount() {
    UUID id = UUID.randomUUID();

    when(scorecardRepository.deleteByIdForCurrentUser(id)).thenReturn(1);

    assertThat(service.deleteById(id)).isEqualTo(1);
  }
}
