package com.alimmit.golf.scorecard;

import static org.assertj.core.api.Assertions.assertThat;
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
  void listAll_returnsMappedDtos() {
    ScorecardEntity entity = entity();
    ScorecardDto expected = dto(UUID.randomUUID());

    when(scorecardRepository.findAllForCurrentUser()).thenReturn(List.of(entity));
    when(scorecardMapper.toDto(entity)).thenReturn(expected);

    assertThat(service.listAll()).containsExactly(expected);
  }

  @Test
  void listAll_emptyResults_returnsEmptyList() {
    when(scorecardRepository.findAllForCurrentUser()).thenReturn(List.of());

    assertThat(service.listAll()).isEmpty();
  }

  @Test
  void listAll_byUserId_delegatesToFindAllCreatedBy() {
    String userId = "user-123";
    ScorecardEntity entity = entity();
    ScorecardDto expected = dto(UUID.randomUUID());

    when(scorecardRepository.findAllCreatedBy(userId)).thenReturn(List.of(entity));
    when(scorecardMapper.toDto(entity)).thenReturn(expected);

    assertThat(service.listAll(userId)).containsExactly(expected);
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
