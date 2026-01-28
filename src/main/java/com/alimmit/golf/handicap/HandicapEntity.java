package com.alimmit.golf.handicap;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "handicap")
@EntityListeners(AuditingEntityListener.class)
class HandicapEntity {

  @Id
  @Column(name = "handicap_id", nullable = false, length = 37)
  private String handicapId;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "golfer_id", nullable = false, updatable = false, length = 36)
  private String golferId;

  @Column(name = "handicap_index", nullable = false, updatable = false)
  private Double handicapIndex;

  @Column(name = "rounds_used", nullable = false, updatable = false)
  private Integer roundsUsed;

  @Column(name = "total_rounds", nullable = false, updatable = false)
  private Integer totalRounds;

  /**
   * Default JPA constructor
   */
  public HandicapEntity() {
  }

  /**
   * Convenience constructor for creating new entities
   *
   * @param handicapId    Handicap record identifier
   * @param golferId      Golfer identifier
   * @param handicapIndex Calculated handicap handicapIndex
   * @param roundsUsed    Number of rounds used to calculate handicap handicapIndex
   * @param totalRounds   Total number of rounds considered
   */
  HandicapEntity(String handicapId, String golferId, Double handicapIndex, Integer roundsUsed, Integer totalRounds) {
    this.handicapId = handicapId;
    this.golferId = golferId;
    this.handicapIndex = handicapIndex;
    this.roundsUsed = roundsUsed;
    this.totalRounds = totalRounds;
  }

  public String getHandicapId() {
    return handicapId;
  }

  public void setHandicapId(String handicapId) {
    this.handicapId = handicapId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public String getGolferId() {
    return golferId;
  }

  public void setGolferId(String golferId) {
    this.golferId = golferId;
  }

  public Double getHandicapIndex() {
    return handicapIndex;
  }

  public void setHandicapIndex(Double handicapIndex) {
    this.handicapIndex = handicapIndex;
  }

  public Integer getRoundsUsed() {
    return roundsUsed;
  }

  public void setRoundsUsed(Integer roundsUsed) {
    this.roundsUsed = roundsUsed;
  }

  public Integer getTotalRounds() {
    return totalRounds;
  }

  public void setTotalRounds(Integer totalRounds) {
    this.totalRounds = totalRounds;
  }
}
