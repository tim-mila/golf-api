package com.alimmit.golf.scorecard;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "scorecard")
@EntityListeners(AuditingEntityListener.class)
public class ScorecardEntity {

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

  @Column(name = "course_id", nullable = false)
  private Long courseId;

  @Column(name = "score", nullable = false)
  private Integer score;

  public ScorecardEntity() {
  }

  public ScorecardEntity(String scorecardId, LocalDate scoreDate, Long courseId, Integer score) {
    this.scorecardId = scorecardId;
    this.scoreDate = scoreDate;
    this.courseId = courseId;
    this.score = score;
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

  public Long getCourseId() {
    return courseId;
  }

  public void setCourseId(Long courseId) {
    this.courseId = courseId;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
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
    return Objects.hashCode(scorecardId);
  }

  @Override
  public String toString() {
    return "ScorecardEntity{" +
        "scorecardId='" + scorecardId + '\'' +
        ", createdAt=" + createdAt +
        ", createdBy='" + createdBy + '\'' +
        ", lastModifiedAt=" + lastModifiedAt +
        ", lastModifiedBy='" + lastModifiedBy + '\'' +
        ", scoreDate=" + scoreDate +
        ", courseId=" + courseId +
        ", score=" + score +
        '}';
  }
}
