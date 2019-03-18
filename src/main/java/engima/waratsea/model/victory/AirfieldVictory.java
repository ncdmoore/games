package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.airfield.AirfieldEventMatcher;
import engima.waratsea.model.game.event.airfield.AirfieldEventMatcherFactory;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.victory.data.AirfieldVictoryData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents an ship victory condition.
 */
@Slf4j
public class AirfieldVictory {

    private GameMap gameMap;

    private AirfieldEventMatcher matcher;

    private int points;              // The points awarded for each occurrence of this victory condition.
    private int totalPoints;         // The total points awarded for all occurrences of this victory condition
    private int requiredPoints;      // The total points required for this victory condition to be met.
                                     // Note, not all victory conditions have a total point requirement.
                                     // In fact most do not have any total point requirement.
    private int requiredOccurrences; // The number of times the underlying event must occur before any points are awarded.
    private int occurrenceCount;     // The number of times the event has occurred before any points are awarded.
                                     // This value is reset every time the required number of occurrences are reached.

    @Getter
    private boolean requirementMet;

    /**
     * Constructor.
     *
     * @param data The victory condition data as read from a JSON file.
     * @param side The side ALLIES or AXIS.
     * @param factory Factory for creating airfield event matchers.
     * @param gameMap The game map.
     */
    @Inject
    public AirfieldVictory(@Assisted final AirfieldVictoryData data,
                       @Assisted final Side side,
                       final AirfieldEventMatcherFactory factory,
                       final GameMap gameMap) {

        matcher = factory.create(data.getEvent());
        points = data.getPoints();
        totalPoints = data.getTotalPoints();
        requiredPoints = data.getRequiredPoints();
        requiredOccurrences = data.getRequiredOccurences();
        occurrenceCount = data.getOccurrenceCount();
        requirementMet = data.isRequirementMet();

        matcher.setSide(side);


        log.info("Victory condition match:");
        matcher.log();
        log.info("Points: {}", points);

        this.gameMap = gameMap;
    }

}
