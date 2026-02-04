package com.alimmit.golf.handicap;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class HandicapMapper {

  private final HandicapIdGenerator handicapIdGenerator;

  HandicapMapper(HandicapIdGenerator handicapIdGenerator) {
    this.handicapIdGenerator = handicapIdGenerator;
  }

  HandicapEntity toEntity(HandicapCalculation dto, String golferId) {
    return new HandicapEntity(
        handicapIdGenerator.generate(),
        golferId,
        dto.handicapIndex(),
        dto.roundsUsed(),
        dto.totalRounds());
  }

  HandicapEntity updateEntity(HandicapEntity existing, HandicapCalculation dto) {
    existing.setHandicapIndex(dto.handicapIndex());
    existing.setRoundsUsed(dto.roundsUsed());
    existing.setTotalRounds(dto.totalRounds());
    return existing;
  }

  Optional<HandicapDto> toOptionalDto(Optional<HandicapEntity> entity) {
    return entity.flatMap(e -> Optional.of(toDto(e)));
  }

  HandicapDto toDto(HandicapEntity entity) {
    return new HandicapDto(
        entity.getHandicapId(),
        entity.getGolferId(),
        entity.getCreatedAt(),
        entity.getHandicapIndex(),
        entity.getRoundsUsed(),
        entity.getTotalRounds()
    );
  }
}
