package com.alimmit.golf.handicap;

import com.alimmit.golf.scorecard.ScorecardEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
class ScorecardEventListener {

  private static final Logger logger = LoggerFactory.getLogger(ScorecardEventListener.class);

  private final HandicapService handicapService;

  ScorecardEventListener(HandicapService handicapService) {
    this.handicapService = handicapService;
  }

  @Async("scorecardEventListenerExecutor")
  @EventListener(ScorecardEvent.class)
  void scorecardCreated(ScorecardEvent event) {
    logger.info("scorecardCreated | event received {}", event);

    if (event.type() == ScorecardEvent.Type.CREATED) {
      handicapService.calculate(event.userId());
    }
  }
}
