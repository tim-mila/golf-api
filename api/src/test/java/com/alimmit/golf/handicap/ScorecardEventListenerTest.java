package com.alimmit.golf.handicap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alimmit.golf.scorecard.ScorecardDto;
import com.alimmit.golf.scorecard.ScorecardEvent;
import com.alimmit.golf.scorecard.ScorecardQueryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScorecardEventListenerTest {

  @Mock private HandicapService handicapService;
  @Mock private ScorecardQueryService scorecardQueryService;

  @InjectMocks private ScorecardEventListener listener;

  private static final String USER_ID = "123";

  @Test
  void createdEvent_triggersHandicapCalculation() {
    List<ScorecardDto> scorecards = List.of();
    when(scorecardQueryService.findAllForUser(USER_ID)).thenReturn(scorecards);

    listener.scorecardEvent(new ScorecardEvent(USER_ID, ScorecardEvent.Type.CREATED));

    verify(scorecardQueryService).findAllForUser(USER_ID);
    verify(handicapService).calculate(scorecards, USER_ID);
  }

  @Test
  void deletedEvent_triggersHandicapCalculation() {
    List<ScorecardDto> scorecards = List.of();
    when(scorecardQueryService.findAllForUser(USER_ID)).thenReturn(scorecards);

    listener.scorecardEvent(new ScorecardEvent(USER_ID, ScorecardEvent.Type.DELETED));

    verify(scorecardQueryService).findAllForUser(USER_ID);
    verify(handicapService).calculate(scorecards, USER_ID);
  }
}
