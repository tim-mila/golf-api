package com.alimmit.golf.handicap;

import com.alimmit.golf.scorecard.ScorecardEvent;
import com.alimmit.golf.scorecard.ScorecardQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
class ScorecardEventListener {

  private static final Logger logger = LoggerFactory.getLogger(ScorecardEventListener.class);

  private final HandicapService handicapService;
  private final ScorecardQueryService scorecardQueryService;

  ScorecardEventListener(
      HandicapService handicapService, ScorecardQueryService scorecardQueryService) {
    this.handicapService = handicapService;
    this.scorecardQueryService = scorecardQueryService;
  }

  @Async("scorecardEventListenerExecutor")
  @EventListener(ScorecardEvent.class)
  void scorecardEvent(ScorecardEvent event) {
    logger.info("scorecardEvent | event received {}", event);

    if (event.type() == ScorecardEvent.Type.CREATED
        || event.type() == ScorecardEvent.Type.DELETED) {
      handicapService.calculate(
          scorecardQueryService.findMostRecentForUser(
              event.userId(), HandicapConstants.HANDICAP_LOOKBACK_ROUNDS),
          event.userId());
    }
  }
}
