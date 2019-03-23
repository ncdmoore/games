package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.event.squadron.SquadronEvent;
import engima.waratsea.model.game.event.squadron.SquadronEventMatcher;
import engima.waratsea.model.game.event.squadron.SquadronEventMatcherFactory;
import engima.waratsea.model.victory.data.SquadronVictoryData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a squadron victory condition.
 */
@Slf4j
public class SquadronVictory implements VictoryCondition<SquadronEvent, SquadronVictoryData> {
    private SquadronEventMatcher matcher;

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
     * The consturctor called by guice.
     *
     * @param data The squadron victory condition data read in from a JSON file.
     * @param factory The squadron event matcher factory.
     */
    @Inject
    public SquadronVictory(@Assisted final SquadronVictoryData data,
                                     final SquadronEventMatcherFactory factory) {

        matcher = factory.create(data.getEvent());
        points = data.getPoints();
        totalPoints = data.getTotalPoints();
        requiredPoints = data.getRequiredPoints();
        requiredOccurrences = data.getRequiredOccurences();
        occurrenceCount = data.getOccurrenceCount();
        requirementMet = data.isRequirementMet();

        log.info("Squadron victory condition match:");
        matcher.log();
        log.info("Points: {}", points);

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
    public int getPoints(final SquadronEvent event) {
        return points;
    }

    /**
     * Determine if the fired event matches this victory condition trigger.
     *
     * @param event The fired event.
     * @return True if the event matches. False otherwise.
     */
    @Override
    public boolean match(final SquadronEvent event) {
        boolean matched = matcher.match(event);

        log.info("Squadron: '{}' '{}'. matched: {}", new Object[]{event.getSquadron().getName(),
                event.getAction(), matched});

        return matched;
    }

    /**
     * Get the victory condition data that is read and written to a JSON file.
     *
     * @return The persistent victory condition data.
     */
    @Override
    public SquadronVictoryData getData() {
        SquadronVictoryData data = new SquadronVictoryData();
        data.setEvent(matcher.getData());
        data.setPoints(points);
        data.setTotalPoints(totalPoints);
        data.setRequiredPoints(requiredPoints);
        data.setRequiredOccurences(requiredOccurrences);
        data.setOccurrenceCount(occurrenceCount);
        data.setRequirementMet(requirementMet);

        return data;
    }
}
