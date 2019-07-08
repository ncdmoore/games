package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.event.airfield.AirfieldEvent;
import engima.waratsea.model.game.event.airfield.AirfieldEventMatcher;
import engima.waratsea.model.game.event.airfield.AirfieldEventMatcherFactory;
import engima.waratsea.model.victory.data.AirfieldVictoryData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents an ship victory condition.
 */
@Slf4j
public class AirfieldVictory implements VictoryCondition<AirfieldEvent, AirfieldVictoryData> {
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
     * @param factory Factory for creating airfield event matchers.
     */
    @Inject
    public AirfieldVictory(@Assisted final AirfieldVictoryData data,
                           final AirfieldEventMatcherFactory factory) {

        matcher = factory.create(data.getEvent());
        points = data.getPoints();
        totalPoints = data.getTotalPoints();
        requiredPoints = data.getRequiredPoints();
        requiredOccurrences = data.getRequiredOccurences();
        occurrenceCount = data.getOccurrenceCount();
        requirementMet = data.isRequirementMet();

        log.debug("Airfield victory condition match:");
        matcher.log();
        log.debug("Points: {}", points);

        // This is needed if no events that trigger this condition are thrown.
        // We must initialize the requirementMet.
        requirementMet = totalPoints >= requiredPoints;
    }

    /**
     * Get the points awarded for this condition.
     *
     * @param event The fired event that triggers the victory award.
     * @return The awarded victory points.
     */
    @Override
    public int getPoints(final AirfieldEvent event) {
        int awardedPoints = 0;

        if (occurrencesMet()) {
            awardedPoints = event.getValue() * points;
            totalPoints += awardedPoints;
            requirementMet = totalPoints >= requiredPoints;
        }

        log.info("Ship: '{}' '{}'. Occurrence: {}, Required: {}. Victory points awarded: {}",
                new Object[]{event.getAirfield().getName(), event.getAction(), occurrenceCount, requiredOccurrences, awardedPoints});

        return awardedPoints;
    }

    /**
     * Determine if the required occurrences are satisfied. Some victory conditions require multiple occurrences of
     * the underlying event to take place before any points are awarded.
     *
     * @return True if the required occurrences are satisfied. False otherwise.
     */
    private boolean occurrencesMet() {
        boolean result = false;
        occurrenceCount++;

        if (requiredOccurrences == 0 || occurrenceCount % requiredOccurrences == 0) {
            result = true;
        }

        return result;
    }

    /**
     * Determine if the fired event matches this victory condition trigger.
     *
     * @param event The fired event.
     * @return True if the event matches. False otherwise.
     */
    @Override
    public boolean match(final AirfieldEvent event) {
        boolean matched = matcher.match(event);

        log.info("Airfield: '{}' '{}'. matched: {}", new Object[]{event.getAirfield().getName(),
                event.getAction(), matched});

        return matched;
    }

    /**
     * Get the victory condition data that is read and written to a JSON file.
     *
     * @return The persistent victory condition data.
     */
    @Override
    public AirfieldVictoryData getData() {
        AirfieldVictoryData data = new AirfieldVictoryData();
        data.setEvent(matcher.getData());
        data.setPoints(points);
        data.setTotalPoints(totalPoints);
        data.setRequiredPoints(requiredPoints);
        data.setRequiredOccurences(requiredOccurrences);
        data.setOccurrenceCount(occurrenceCount);
        data.setRequirementMet(requirementMet);

        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {
    }
}
