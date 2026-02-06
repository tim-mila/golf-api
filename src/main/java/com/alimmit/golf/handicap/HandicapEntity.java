package com.alimmit.golf.handicap;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.Generated;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "handicap")
@EntityListeners(AuditingEntityListener.class)
@Audited
class HandicapEntity {

  @Id
  @Generated(sql = "uuidv7()", writable = true)
  @Column(
      name = "handicap_id",
      nullable = false,
      updatable = false,
      columnDefinition = "UUID DEFAULT uuidv7()")
  private UUID handicapId;

  @CreatedDate
  @NotAudited
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "last_modified_at", nullable = false)
  private Instant lastModifiedAt;

  @Column(name = "golfer_id", unique = true, nullable = false, updatable = false, length = 36)
  private String golferId;

  @Column(name = "handicap_index", nullable = false)
  private Double handicapIndex;

  @Column(name = "rounds_used", nullable = false)
  private Integer roundsUsed;

  @Column(name = "total_rounds", nullable = false)
  private Integer totalRounds;

  public void setHandicapIndex(Double handicapIndex) {
    this.handicapIndex = handicapIndex;
  }

  public void setRoundsUsed(Integer roundsUsed) {
    this.roundsUsed = roundsUsed;
  }

  public void setTotalRounds(Integer totalRounds) {
    this.totalRounds = totalRounds;
  }

  /** Default JPA constructor */
  public HandicapEntity() {}

  /**
   * Convenience constructor for creating new entities
   *
   * @param golferId Golfer identifier
   * @param handicapIndex Calculated handicap handicapIndex
   * @param roundsUsed Number of rounds used to calculate handicap handicapIndex
   * @param totalRounds Total number of rounds considered
   */
  HandicapEntity(String golferId, Double handicapIndex, Integer roundsUsed, Integer totalRounds) {
    this.golferId = golferId;
    this.handicapIndex = handicapIndex;
    this.roundsUsed = roundsUsed;
    this.totalRounds = totalRounds;
  }

  public UUID getHandicapId() {
    return handicapId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getLastModifiedAt() {
    return lastModifiedAt;
  }

  public String getGolferId() {
    return golferId;
  }

  public Double getHandicapIndex() {
    return handicapIndex;
  }

  public Integer getRoundsUsed() {
    return roundsUsed;
  }

  public Integer getTotalRounds() {
    return totalRounds;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    HandicapEntity entity = (HandicapEntity) o;
    return Objects.equals(handicapId, entity.handicapId)
        && Objects.equals(createdAt, entity.createdAt)
        && Objects.equals(lastModifiedAt, entity.lastModifiedAt)
        && Objects.equals(golferId, entity.golferId)
        && Objects.equals(handicapIndex, entity.handicapIndex)
        && Objects.equals(roundsUsed, entity.roundsUsed)
        && Objects.equals(totalRounds, entity.totalRounds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        handicapId, createdAt, lastModifiedAt, golferId, handicapIndex, roundsUsed, totalRounds);
  }

  @Override
  public String toString() {
    return "HandicapEntity{"
        + "handicapId='"
        + handicapId
        + '\''
        + ", golferId='"
        + golferId
        + '\''
        + ", handicapIndex="
        + handicapIndex
        + '}';
  }
}
