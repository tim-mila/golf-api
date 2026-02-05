package com.alimmit.golf.scorecard;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
class ScorecardEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public ScorecardEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  void publishCreated(ScorecardDto scorecardDto) {
    applicationEventPublisher.publishEvent(
        new ScorecardEvent(scorecardDto.createdBy(), ScorecardEvent.Type.CREATED));
  }

  void publishedDeleted() {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    applicationEventPublisher.publishEvent(new ScorecardEvent(name, ScorecardEvent.Type.DELETED));
  }
}
