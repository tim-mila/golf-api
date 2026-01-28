package com.alimmit.golf.scorecard;

public record ScorecardEvent(
        String scorecardId,
        String userId,
        Type type
) {

    public enum Type {
        CREATED
    }
}
