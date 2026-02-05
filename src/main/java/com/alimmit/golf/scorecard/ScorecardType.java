package com.alimmit.golf.scorecard;

public enum ScorecardType {
  NINE(9), EIGHTEEN(18);

  private final int holesPlayed;

  ScorecardType(int holesPlayed) {
    this.holesPlayed = holesPlayed;
  }

  public int getHolesPlayed() {
    return holesPlayed;
  }
}
