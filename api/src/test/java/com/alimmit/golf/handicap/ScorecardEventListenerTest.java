package com.alimmit.golf.handicap;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alimmit.golf.scorecard.ScorecardDto;
import com.alimmit.golf.scorecard.ScorecardEvent;
import com.alimmit.golf.scorecard.ScorecardService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScorecardEventListenerTest {

  @Mock private HandicapService handicapService;
  @Mock private ScorecardService scorecardService;

  @InjectMocks private ScorecardEventListener listener;

  private static final String USER_ID = "123";

  @Test
  void createdEvent_triggersHandicapCalculation() {
    List<ScorecardDto> scorecards = List.of();
    when(scorecardService.listAll(USER_ID)).thenReturn(scorecards);

    listener.scorecardEvent(new ScorecardEvent(USER_ID, ScorecardEvent.Type.CREATED));

    verify(scorecardService).listAll(USER_ID);
    verify(handicapService).calculate(scorecards, USER_ID);
  }

  @Test
  void deletedEvent_triggersHandicapCalculation() {
    List<ScorecardDto> scorecards = List.of();
    when(scorecardService.listAll(USER_ID)).thenReturn(scorecards);

    listener.scorecardEvent(new ScorecardEvent(USER_ID, ScorecardEvent.Type.DELETED));

    verify(scorecardService).listAll(USER_ID);
    verify(handicapService).calculate(scorecards, USER_ID);
  }
}
