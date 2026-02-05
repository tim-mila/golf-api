package com.alimmit.golf.scorecard;

public record ScorecardEvent(String userId, Type type) {

  public enum Type {
    CREATED,
    DELETED
  }
}
