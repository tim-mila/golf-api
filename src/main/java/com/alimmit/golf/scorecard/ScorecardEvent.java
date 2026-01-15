package com.alimmit.golf.scorecard;

public record ScorecardEvent(
        String scorecardId,
        Type type
) {

    public enum Type {
        CREATED
    }
}
