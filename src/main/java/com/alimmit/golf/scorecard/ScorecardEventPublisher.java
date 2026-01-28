package com.alimmit.golf.scorecard;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
class ScorecardEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public ScorecardEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    void publishCreated(ScorecardDto scorecardDto) {
        applicationEventPublisher.publishEvent(new ScorecardEvent(scorecardDto.scorecardId(), scorecardDto.createdBy(), ScorecardEvent.Type.CREATED));
    }
}
