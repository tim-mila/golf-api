package com.alimmit.golf.scorecard;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.Generated;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "scorecard")
@EntityListeners(AuditingEntityListener.class)
class ScorecardEntity {

  @Id
  @Generated(sql = "uuidv7()", writable = true)
  @Column(
      name = "scorecard_id",
      updatable = false,
      nullable = false,
      columnDefinition = "UUID DEFAULT uuidv7()")
  private UUID id;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @CreatedBy
  @Column(nullable = false, updatable = false)
  private String createdBy;

  @Column(nullable = false, updatable = false)
  private LocalDate scoreDate;

  @Column(nullable = false, updatable = false)
  private String courseName;

  @Column(nullable = false, updatable = false)
  private String teeName;

  @Column(nullable = false, updatable = false)
  private int score;

  @Column(nullable = false, updatable = false)
  private int par;

  @Column(nullable = false, updatable = false)
  private double rating;

  @Column(nullable = false, updatable = false)
  private double slope;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false)
  private ScorecardType scorecardType;

  @Column(nullable = false, updatable = false)
  private double differential;

  @Column(nullable = false, updatable = false)
  private boolean indexEstablished;

  public ScorecardEntity() {}

  public ScorecardEntity(
      LocalDate scoreDate,
      String courseName,
      String teeName,
      Integer score,
      Integer par,
      Double rating,
      Double slope,
      ScorecardType scorecardType,
      double differential,
      boolean indexEstablished) {
    this.scoreDate = scoreDate;
    this.courseName = courseName;
    this.teeName = teeName;
    this.score = score;
    this.par = par;
    this.rating = rating;
    this.slope = slope;
    this.scorecardType = scorecardType;
    this.differential = differential;
    this.indexEstablished = indexEstablished;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public LocalDate getScoreDate() {
    return scoreDate;
  }

  public String getCourseName() {
    return courseName;
  }

  public String getTeeName() {
    return teeName;
  }

  public int getScore() {
    return score;
  }

  public int getPar() {
    return par;
  }

  public double getRating() {
    return rating;
  }

  public double getSlope() {
    return slope;
  }

  public ScorecardType getScorecardType() {
    return scorecardType;
  }

  public double getDifferential() {
    return differential;
  }

  public boolean isIndexEstablished() {
    return indexEstablished;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ScorecardEntity that = (ScorecardEntity) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, createdAt, createdBy, scoreDate, courseName, score, par, rating, slope, scorecardType);
  }
}
