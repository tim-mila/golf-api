package com.alimmit.golf.differential;

import com.alimmit.golf.handicap.HandicapDto;
import com.alimmit.golf.handicap.HandicapService;
import com.alimmit.golf.scorecard.ScorecardRequestDto;
import com.alimmit.golf.scorecard.ScorecardType;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
class DifferentialCalculatorImpl implements DifferentialCalculator {

  private static final double STANDARD_SLOPE = 113.0;

  private final HandicapService handicapService;

  DifferentialCalculatorImpl(HandicapService handicapService) {
    this.handicapService = handicapService;
  }

  @Override
  public Differential calculateDifferential(ScorecardRequestDto scorecard) {

    Differential differential;

    // The standard differential is needed for both nine and 18 hole scores
    double standardDifferential = standardDifferential(scorecard);

    Optional<HandicapDto> handicap = handicapService.getHandicap();

    // For 18 hole scores, use the standard differential
    if (ScorecardType.EIGHTEEN == scorecard.scorecardType()) {
      differential = new Differential(standardDifferential, handicap.isPresent());
    }
    // For nine hole scores with an established handicap index convert to an 18 hole
    // differential using the expected score.
    // https://www.usga.org/content/usga/home-page/handicapping/world-handicap-system/2024-revision/2024-treatment-of-9-hole-scores-FAQ.html
    else if (ScorecardType.NINE == scorecard.scorecardType() && handicap.isPresent()) {
      double expectedDifferential =
          expectedDifferential(handicap.map(HandicapDto::handicapIndex).orElse(0.0));
      differential = new Differential(standardDifferential + expectedDifferential, true);
    }
    // For nine hole scores without an established handicap index use the standard differential
    else {
      differential = new Differential(standardDifferential, false);
    }

    return differential;
  }

  /**
   * Calculate score differential for a single round. Formula: (113 / Slope Rating) × (Score -
   * Course Rating)
   */
  private double standardDifferential(ScorecardRequestDto scorecard) {
    double differential =
        (STANDARD_SLOPE / scorecard.slope()) * (scorecard.score() - scorecard.rating());
    // Round to one decimal place
    return Math.round(differential * 10.0) / 10.0;
  }

  /**
   * Calculate the expected differential, used for nine hole scores after a handicap index is
   * established Formula: handicap index * 0.52 + 1.2
   *
   * @return Expected differential if handicap established
   * @see <a
   *     href="https://www.usga.org/content/dam/usga/images/handicapping/infographics/Understanding%20Expected%20Score%20Infographic.pdf">https://www.usga.org/content/dam/usga/images/handicapping/infographics/Understanding%20Expected%20Score%20Infographic.pdf</a>
   */
  private double expectedDifferential(Double index) {
    double differential = index * 0.52 + 1.2;
    // Round to one decimal place
    return Math.round(differential * 10.0) / 10.0;
  }
}
