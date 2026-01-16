package com.alimmit.golf.handicap;

import com.alimmit.golf.scorecard.ScorecardEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
class ScorecardEventListener {

    private static final System.Logger log = System.getLogger("ScorecardEventListener");

    @Async("scorecardEventListenerExecutor")
    @EventListener(ScorecardEvent.class)
    void scorecardCreated(ScorecardEvent event) {
        log.log(System.Logger.Level.INFO, "Scorecard Created {}", event);
    }
}
