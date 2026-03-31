package com.alimmit.golf.course;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.Generated;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.generator.EventType;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Audited
@Table(name = "tee")
@EntityListeners(AuditingEntityListener.class)
class TeeEntity {

  @Id
  @Generated(event = EventType.INSERT)
  @Column(
      name = "tee_id",
      unique = true,
      updatable = false,
      nullable = false,
      columnDefinition = "UUID DEFAULT uuidv7()")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false, updatable = false)
  private CourseEntity course;

  @CreatedDate
  @NotAudited
  @Column(updatable = false, nullable = false)
  private Instant createdAt;

  @CreatedBy
  @NotAudited
  @Column(updatable = false, nullable = false)
  private String createdBy;

  @LastModifiedDate
  @Column(nullable = false)
  private Instant lastModifiedAt;

  @LastModifiedBy
  @Column(nullable = false)
  private String lastModifiedBy;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Integer par;

  @Column(nullable = false, precision = 4, scale = 1)
  private BigDecimal slope;

  @Column(nullable = false, precision = 4, scale = 1)
  private BigDecimal rating;

  public TeeEntity() {}

  TeeEntity(CourseEntity course, String name, Integer par, BigDecimal slope, BigDecimal rating) {
    this.course = course;
    this.name = name;
    this.par = par;
    this.slope = slope;
    this.rating = rating;
  }

  public UUID getId() {
    return id;
  }

  public CourseEntity getCourse() {
    return course;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getLastModifiedAt() {
    return lastModifiedAt;
  }

  public String getName() {
    return name;
  }

  public Integer getPar() {
    return par;
  }

  public BigDecimal getSlope() {
    return slope;
  }

  public BigDecimal getRating() {
    return rating;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPar(Integer par) {
    this.par = par;
  }

  public void setSlope(BigDecimal slope) {
    this.slope = slope;
  }

  public void setRating(BigDecimal rating) {
    this.rating = rating;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    TeeEntity that = (TeeEntity) o;
    return Objects.equals(id, that.id)
        && Objects.equals(name, that.name)
        && Objects.equals(par, that.par)
        && Objects.equals(slope, that.slope)
        && Objects.equals(rating, that.rating);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, par, slope, rating);
  }

  @Override
  public String toString() {
    return "TeeEntity{"
        + "name='"
        + name
        + '\''
        + ", par="
        + par
        + ", slope="
        + slope
        + ", rating="
        + rating
        + '}';
  }
}
