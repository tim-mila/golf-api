package com.alimmit.golf.handicap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alimmit.golf.errors.NotFoundException;
import com.alimmit.golf.scorecard.ScorecardDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.data.history.Revisions;

@ExtendWith(MockitoExtension.class)
class JpaHandicapServiceImplTest {

  @Mock private HandicapCalculator handicapCalculator;
  @Mock private HandicapMapper handicapMapper;
  @Mock private HandicapRepository handicapRepository;

  private JpaHandicapServiceImpl handicapService;

  @BeforeEach
  void setUp() {
    handicapService =
        new JpaHandicapServiceImpl(handicapCalculator, handicapMapper, handicapRepository);
  }

  @Test
  void calculate_whenCalculatorReturnsEmpty_doesNotSave() {
    List<ScorecardDto> scorecards = List.of();
    when(handicapCalculator.calculate(scorecards)).thenReturn(Optional.empty());

    handicapService.calculate(scorecards, "user-123");

    verify(handicapRepository, never()).save(any());
  }

  @Test
  void calculate_whenNoExistingHandicap_savesNewEntity() {
    String golferId = "user-123";
    List<ScorecardDto> scorecards = List.of();
    HandicapCalculation calculation = createCalculation(12.3, 3, 3);
    HandicapEntity newEntity = new HandicapEntity(golferId, 12.3, 3, 3);

    when(handicapCalculator.calculate(scorecards)).thenReturn(Optional.of(calculation));
    when(handicapRepository.findHandicapForUser(golferId)).thenReturn(Optional.empty());
    when(handicapMapper.toEntity(calculation, golferId)).thenReturn(newEntity);

    handicapService.calculate(scorecards, golferId);

    verify(handicapRepository).save(newEntity);
  }

  @Test
  void calculate_whenExistingHandicap_updatesAndSavesEntity() {
    String golferId = "user-123";
    List<ScorecardDto> scorecards = List.of();
    HandicapCalculation calculation = createCalculation(11.1, 4, 5);
    HandicapEntity existingEntity = new HandicapEntity(golferId, 12.3, 3, 3);
    HandicapEntity updatedEntity = new HandicapEntity(golferId, 11.1, 4, 5);

    when(handicapCalculator.calculate(scorecards)).thenReturn(Optional.of(calculation));
    when(handicapRepository.findHandicapForUser(golferId)).thenReturn(Optional.of(existingEntity));
    when(handicapMapper.updateEntity(existingEntity, calculation)).thenReturn(updatedEntity);

    handicapService.calculate(scorecards, golferId);

    verify(handicapRepository).save(updatedEntity);
  }

  @Test
  void getHandicap() {
    HandicapEntity entity = new HandicapEntity("user-123", 12.3, 3, 3);
    HandicapDto dto = createDto(entity);
    Optional<HandicapEntity> optionalEntity = Optional.of(entity);

    when(handicapRepository.findHandicapForCurrentUser()).thenReturn(optionalEntity);
    when(handicapMapper.toDto(optionalEntity)).thenReturn(dto);

    HandicapDto result = handicapService.getHandicap();

    assertThat(result).isEqualTo(dto);
    assertThat(result.established()).isTrue();
  }

  @Test
  void getHandicapNotEstablished() {
    Optional<HandicapEntity> empty = Optional.empty();

    when(handicapRepository.findHandicapForCurrentUser()).thenReturn(empty);
    when(handicapMapper.toDto(empty)).thenReturn(HandicapDto.unestablished());

    HandicapDto result = handicapService.getHandicap();

    assertThat(result.established()).isFalse();
    assertThat(result.handicapId()).isNull();
    assertThat(result.handicapIndex()).isNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void getHistory() {
    HandicapEntity entity = new HandicapEntity("user-123", 12.3, 3, 3);
    HandicapRevisionDto revisionDto = createRevisionDto();
    Revision<Integer, HandicapEntity> revision = mock(Revision.class);
    Revisions<Integer, HandicapEntity> revisions = Revisions.of(List.of(revision));

    when(handicapRepository.findHandicapForCurrentUser()).thenReturn(Optional.of(entity));
    when(handicapRepository.findRevisions(any())).thenReturn(revisions);
    when(handicapMapper.toRevision(revision)).thenReturn(revisionDto);

    List<HandicapRevisionDto> result = handicapService.getHistory();

    assertThat(result).containsExactly(revisionDto);
  }

  @Test
  void getHistoryNotFound() {
    when(handicapRepository.findHandicapForCurrentUser()).thenReturn(Optional.empty());

    assertThatThrownBy(() -> handicapService.getHistory()).isInstanceOf(NotFoundException.class);
  }

  private HandicapCalculation createCalculation(Double index, int roundsUsed, int totalRounds) {
    return new HandicapCalculation(index, roundsUsed, totalRounds, List.of(), Instant.now());
  }

  private HandicapDto createDto(HandicapEntity entity) {
    return new HandicapDto(
        true,
        entity.getId(),
        entity.getGolferId(),
        entity.getCreatedAt(),
        entity.getLastModifiedAt(),
        entity.getHandicapIndex(),
        entity.getRoundsUsed(),
        entity.getTotalRounds());
  }

  private HandicapRevisionDto createRevisionDto() {
    return new HandicapRevisionDto(
        UUID.randomUUID(),
        "user-123",
        Instant.now(),
        Instant.now(),
        12.3,
        3,
        3,
        1,
        RevisionMetadata.RevisionType.INSERT,
        Instant.now());
  }
}
