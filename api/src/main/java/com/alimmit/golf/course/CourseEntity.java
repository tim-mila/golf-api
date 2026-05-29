package com.alimmit.golf.course;

import jakarta.persistence.*;
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
@Table(name = "course")
@EntityListeners(AuditingEntityListener.class)
class CourseEntity {

  @Id
  @Generated(event = EventType.INSERT)
  @Column(
      name = "course_id",
      unique = true,
      updatable = false,
      nullable = false,
      columnDefinition = "UUID DEFAULT uuidv7()")
  private UUID id;

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
  private String club;

  @Column(nullable = false)
  private String course;

  @Column(nullable = false)
  private String city;

  @Enumerated(value = EnumType.STRING)
  @Column(nullable = false)
  private USState state;

  public CourseEntity() {}

  CourseEntity(String club, String course, String city, USState state) {
    this.club = club;
    this.course = course;
    this.city = city;
    this.state = state;
  }

  public UUID getId() {
    return id;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getLastModifiedAt() {
    return lastModifiedAt;
  }

  public String getClub() {
    return club;
  }

  public String getCourse() {
    return course;
  }

  public String getCity() {
    return city;
  }

  public USState getState() {
    return state;
  }

  public void setClub(String club) {
    this.club = club;
  }

  public void setCourse(String course) {
    this.course = course;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public void setState(USState state) {
    this.state = state;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CourseEntity that = (CourseEntity) o;
    return Objects.equals(id, that.id)
        && Objects.equals(club, that.club)
        && Objects.equals(course, that.course)
        && Objects.equals(city, that.city)
        && Objects.equals(state, that.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, club, course, city, state);
  }

  @Override
  public String toString() {
    return "CourseEntity{"
        + "club='"
        + club
        + '\''
        + ", course='"
        + course
        + '\''
        + ", city='"
        + city
        + '\''
        + ", state='"
        + state
        + '\''
        + '}';
  }
}
