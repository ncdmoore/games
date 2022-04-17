package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.event.squadron.SquadronEvent;
import engima.waratsea.model.game.event.squadron.SquadronEventMatcher;
import engima.waratsea.model.game.event.squadron.SquadronEventMatcherFactory;
import engima.waratsea.model.victory.data.SquadronVictoryData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a squadron victory condition.
 *
 * Typically, an event occurs for each step that results in victory conditions being awarded.
 * For example, by default any step of enemy aircraft destroyed in combat results in 6
 * victory points being awarded. Thus, if a full squadron is destroyed in combat then 2
 * squadron victory events should be fired: one for each step. This results in 12 total
 * victory points.
 */
@Slf4j
public class SquadronVictory implements VictoryCondition<SquadronEvent, SquadronVictoryData> {
    private final SquadronEventMatcher matcher;

    private final String title;
    private final String description;
    private final String award;
    private final int points;              // The points awarded for each occurrence of this victory condition.
    private final int totalPoints;         // The total points awarded for all occurrences of this victory condition
    private final int requiredPoints;      // The total points required for this victory condition to be met.
                                           // Note, not all victory conditions have a total point requirement.
                                           // In fact most do not have any total point requirement.
    private final int requiredOccurrences; // The number of times the underlying event must occur before any points are awarded.
    private final int occurrenceCount;     // The number of times the event has occurred before any points are awarded.
                                           // This value is reset every time the required number of occurrences are reached.

    @Getter
    private boolean requirementMet;

    /**
     * The constructor called by guice.
     *
     * @param data The squadron victory condition data read in from a JSON file.
     * @param factory The squadron event matcher factory.
     */
    @Inject
    public SquadronVictory(@Assisted final SquadronVictoryData data,
                                     final SquadronEventMatcherFactory factory) {

        title = data.getTitle();
        description = data.getDescription();
        award = data.getAward();
        matcher = factory.create(data.getEvent());
        points = data.getPoints();
        totalPoints = data.getTotalPoints();
        requiredPoints = data.getRequiredPoints();
        requiredOccurrences = data.getRequiredOccurences();
        occurrenceCount = data.getOccurrenceCount();
        requirementMet = data.isRequirementMet();

        log.debug("Squadron victory condition match:");
        matcher.log();
        log.debug("Points: {}", points);

        // This is needed if no events that trigger this condition are thrown.
        // We must initialize the requirementMet.
        requirementMet = totalPoints >= requiredPoints;
    }


    /**
     * Indicates if the victory condition total point requirement is met.
     *
     * @param awardedPoints The points awarded for the event.
     * @return True if the point requirement is met. False otherwise.
     */
    @Override
    public boolean isPointRequirementMet(final int awardedPoints) {
        return true;
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
        data.setTitle(title);
        data.setDescription(description);
        data.setAward(award);
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
     * Get the victory conditions details.
     *
     * @return The victory conditions details in generic form.
     */
    @Override
    public VictoryConditionDetails getDetails() {
        Map<String, String> info = new LinkedHashMap<>();

        VictoryConditionDetails details = new VictoryConditionDetails();

        details.setKey(title);
        details.setInfo(info);

        info.put("Description:", description);
        info.put("Award:", award);
        info.put("Action:", matcher.getActionString());
        info.put("Squadron Names:", matcher.getSquadronNamesString());
        info.put("Side:", matcher.getSide().toString());
        info.put("Aircraft model", matcher.getAircraftModelString());
        info.put("Aircraft Type:", matcher.getAircraftTypeString());
        info.put("Location:", matcher.getLocationsString());
        info.put("Port Origin:", matcher.getAirfieldOriginString());
        info.put("Required Points:", requiredPoints + "");
        info.put("Required Occurrences:", requiredOccurrences + "");

        return details;
    }
}
