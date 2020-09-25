package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.ShipEventMatcherFactory;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.victory.data.ShipVictoryData;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class represents a victory condition that must be matched in order for the player to win the game.
 */
@Slf4j
public class RequiredShipVictory implements VictoryCondition<ShipEvent, ShipVictoryData> {

    private final GameMap gameMap;

    private final String title;
    private final String description;
    private final List<ShipEventMatcher> matchers;

    private int totalPoints;            //Used just to ensure the required points are met. This does not count toward total victory score.
                                        //The ship victory points will be stored in the ShipVictory class.
    private final int requiredPoints;   //The number of points required for this victory condition to be satisfied.

    @Getter
    private boolean requirementMet;

    /**
     * Constructor.
     *
     * @param data The victory condition data as read from a JSON file.
     * @param factory Factory for creating ship event matchers.
     * @param gameMap The game map.
     */
    @Inject
    public RequiredShipVictory(@Assisted final ShipVictoryData data,
                                         final ShipEventMatcherFactory factory,
                                         final GameMap gameMap) {
        this.gameMap = gameMap;

        matchers = data.getEvents()
                .stream()
                .map(factory::create)
                .collect(Collectors.toList());

        title = data.getTitle();
        description = data.getDescription();
        totalPoints = data.getTotalPoints();
        requiredPoints = data.getRequiredPoints();

        requirementMet = data.isRequirementMet();

        log.debug("Required victory condition match set:");
        matchers.forEach(ShipEventMatcher::log);

    }

    /**
     * Get the victory data that is read and written. This is all the data that needs to persist for this class.
     *
     * @return The corresponding persistent victory data.
     */
    @Override
    public ShipVictoryData getData() {
        ShipVictoryData data = new ShipVictoryData();
        data.setTitle(title);
        data.setDescription(description);
        data.setEvents(PersistentUtility.getData(matchers));
        data.setRequirementMet(requirementMet);
        data.setTotalPoints(totalPoints);
        data.setRequiredPoints(requiredPoints);
        return data;
    }

    /**
     * Get the victory conditions details.
     *
     * @return The victory conditions detials in generic form.
     */
    @Override
    public VictoryConditionDetails getDetails() {
        Map<String, String> info = new LinkedHashMap<>();

        VictoryConditionDetails details = new VictoryConditionDetails();

        details.setKey(title);
        details.setInfo(info);

        info.put("Description:", description);
        info.putAll(getShipEventInfo());
        info.put("Required Points:", requiredPoints + "");

        return details;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {
    }

    /**
     * Determine if a ship event thrown results in meeting the required victory condition.
     *
     * @param event The fired ship event.
     * @return True if the requirement is met. False otherwise.
     */
    public boolean match(final ShipEvent event) {
        // Check all event matchers. If any one event matcher matches the fired event
        // then the victory condition is met.
        boolean matched = matchers
                .stream()
                .anyMatch(matcher -> matcher.match(event));

        String location = gameMap.convertPortReferenceToName(event.getShip().getTaskForce().getReference());

        log.info("Ship '{}' '{}' at reference '{}' matched: '{}'",
                new Object[] {event.getShip().getName(), event.getAction(), location, matched});

        return matched;
    }

    /**
     * Indicates if the victory condition total point requirement is met.
     *
     * @param awardedPoints The points awarded for the event.
     * @return True if the point requirement is met. False otherwise.
     */
    @Override
    public boolean isPointRequirementMet(final int awardedPoints) {
        totalPoints += awardedPoints;

        requirementMet = totalPoints >= requiredPoints;

        log.info("Points awarded: '{}', New total points: '{}', Required total points: '{}', met: '{}'",
                new Object[] {awardedPoints, totalPoints, requiredPoints, requirementMet});

        return requirementMet;
    }

    /**
     * Get the victory points awarded when this condition is met.
     *
     * @return The victory points awarded.
     */
    @Override
    public int getPoints(final ShipEvent event) {
        return 0;
    }

    private Map<String, String> getShipEventInfo() {
        return matchers
                .stream()
                .map(this::getEventDetails)
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> String.join(",", v1, v2), LinkedHashMap::new));
    }

    private Map<String, String> getEventDetails(final ShipEventMatcher matcher) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Action:", matcher.getActionString());
        map.put("Ship names:", matcher.getShipNamesString());
        map.put("Side:", matcher.getSide().toString());
        map.put("Nation:", matcher.getNationString());
        map.put("Ship Types:", matcher.getShipTypesString());
        map.put("Location:", matcher.getLocationsString());
        map.put("Port Origin:", matcher.getPortOriginString());
        return map;
    }

}
