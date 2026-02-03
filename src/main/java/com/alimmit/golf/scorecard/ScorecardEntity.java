package com.alimmit.golf.scorecard;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

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

  @Column(name = "score_date", nullable = false, updatable = false)
  private LocalDate scoreDate;

  @Column(name = "course_name", nullable = false, updatable = false)
  private String courseName;

  @Column(name = "tee_name", nullable = false, updatable = false)
  private String teeName;

  @Column(name = "score", nullable = false, updatable = false)
  private int score;

  @Column(name = "rating", nullable = false, updatable = false)
  private double rating;

  @Column(name = "slope", nullable = false, updatable = false)
  private double slope;

  @Enumerated(EnumType.STRING)
  @Column(name = "scorecard_type", nullable = false, updatable = false)
  private ScorecardType scorecardType;

  @Column(name = "differential", nullable = false, updatable = false)
  private double differential;

  @Column(name = "index_established", nullable = false, updatable = false)
  private boolean indexEstablished;

  public ScorecardEntity() {
  }

  public ScorecardEntity(
      String scorecardId,
      LocalDate scoreDate,
      String courseName,
      String teeName,
      Integer score,
      Double rating,
      Double slope,
      ScorecardType scorecardType,
      double differential,
      boolean indexEstablished) {
    this.scorecardId = scorecardId;
    this.scoreDate = scoreDate;
    this.courseName = courseName;
    this.teeName = teeName;
    this.score = score;
    this.rating = rating;
    this.slope = slope;
    this.scorecardType = scorecardType;
    this.differential = differential;
    this.indexEstablished = indexEstablished;
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

  public String getTeeName() {
    return teeName;
  }

  public void setTeeName(String teeName) {
    this.teeName = teeName;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }

  public double getSlope() {
    return slope;
  }

  public void setSlope(double slope) {
    this.slope = slope;
  }

  public ScorecardType getScorecardType() {
    return scorecardType;
  }

  public void setScorecardType(ScorecardType scorecardType) {
    this.scorecardType = scorecardType;
  }

  public double getDifferential() {
    return differential;
  }

  public void setDifferential(double differential) {
    this.differential = differential;
  }

  public boolean isIndexEstablished() {
    return indexEstablished;
  }

  public void setIndexEstablished(boolean indexEstablished) {
    this.indexEstablished = indexEstablished;
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
        scoreDate,
        courseName,
        score,
        rating,
        slope,
        scorecardType);
  }
}
