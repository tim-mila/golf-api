package com.alimmit.golf.scorecard;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "scorecard")
@EntityListeners(AuditingEntityListener.class)
class ScorecardEntity {

  @Id
  @Column(name = "scorecard_id", nullable = false, length = 36)
  private String scorecardId;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false, length = 255)
  private String createdBy;

  @LastModifiedDate
  @Column(name = "last_modified_at")
  private Instant lastModifiedAt;

  @LastModifiedBy
  @Column(name = "last_modified_by", length = 255)
  private String lastModifiedBy;

  @Column(name = "score_date", nullable = false)
  private LocalDate scoreDate;

  @Column(name = "course_name", nullable = false)
  private String courseName;

  @Column(name = "score", nullable = false)
  private Integer score;

  @Column(name = "rating", nullable = false)
  private Double rating;

  @Column(name = "slope", nullable = false)
  private Double slope;

  @Enumerated(EnumType.STRING)
  @Column(name = "scorecard_type", nullable = false, updatable = false)
  private ScorecardType scorecardType;

  public ScorecardEntity() {
  }

  public ScorecardEntity(
      String scorecardId,
      LocalDate scoreDate,
      String courseName,
      Integer score,
      Double rating,
      Double slope,
      ScorecardType scorecardType) {
    this.scorecardId = scorecardId;
    this.scoreDate = scoreDate;
    this.courseName = courseName;
    this.score = score;
    this.rating = rating;
    this.slope = slope;
    this.scorecardType = scorecardType;
  }

  public String getScorecardId() {
    return scorecardId;
  }

  public void setScorecardId(String scorecardId) {
    this.scorecardId = scorecardId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Instant getLastModifiedAt() {
    return lastModifiedAt;
  }

  public void setLastModifiedAt(Instant lastModifiedAt) {
    this.lastModifiedAt = lastModifiedAt;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public LocalDate getScoreDate() {
    return scoreDate;
  }

  public void setScoreDate(LocalDate scoreDate) {
    this.scoreDate = scoreDate;
  }

  public String getCourseName() {
    return courseName;
  }

  public void setCourseName(String courseName) {
    this.courseName = courseName;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  public Double getRating() {
    return rating;
  }

  public void setRating(Double rating) {
    this.rating = rating;
  }

  public Double getSlope() {
    return slope;
  }

  public void setSlope(Double slopeRating) {
    this.slope = slopeRating;
  }

  public ScorecardType getScoreType() {
    return scorecardType;
  }

  public void setScoreType(ScorecardType scorecardType) {
    this.scorecardType = scorecardType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ScorecardEntity that = (ScorecardEntity) o;
    return Objects.equals(scorecardId, that.scorecardId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        scorecardId,
        createdAt,
        createdBy,
        lastModifiedAt,
        lastModifiedBy,
        scoreDate,
        courseName,
        score,
        rating,
        slope,
        scorecardType);
  }
}
