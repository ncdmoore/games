package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.victory.data.ShipVictoryData;
import engima.waratsea.model.victory.data.VictoryConditionsData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Represents a side's victory conditions for the game.
 * Thus, there are only two instances of this class constructed.
 */
@Slf4j
public class VictoryConditions {

    private Side side;

    @Getter
    private int totalVictoryPoints;

    @Getter
    private String objectives;

    private List<ShipVictoryCondition> defaultShips;    // The default victory conditions for ship.
    private List<ShipVictoryCondition> scenarioShips;   // Scenario specific victory conditions for ships. These may override the default.
    private List<ShipVictoryCondition> requiredShips;   // Scenario required victory conditions.

    private static ShipVictoryFactory shipVictoryFactory;
    private static BiFunction<ShipVictoryData, Side, ShipVictoryCondition> createShipVictory = (d, s) -> shipVictoryFactory.createShip(d, s);
    private static BiFunction<ShipVictoryData, Side, ShipVictoryCondition> createRequiredShipVictory = (d, s) -> shipVictoryFactory.createRequired(d, s);

    /**
     * Holds the result of a victory condition check.
     */
    private static class Result {
        @Getter
        @Setter
        private boolean awarded;

        @Getter
        @Setter
        private int points;
    }

    /**
     * Stores the history of events that award victory points.
     */
    private static class History {
        @Getter
        private final GameEvent event;

        @Getter
        private final int points;

        /**
         * Constructor.
         *
         * @param event The game event.
         * @param points The corresponding points awarded.
         */
        History(final GameEvent event, final int points) {
            this.event = event;
            this.points = points;
        }
    }

    private List<History> history = new ArrayList<>();

    private boolean registered = false;

    /**
     * Constructor of the a side's victory.
     *
     * @param data The victory data read in from a JSON file.
     * @param side The side ALLIES or AXIS of the victory conditions.
     * @param shipVF Ship victory factory.
     */
    @Inject
    public VictoryConditions(@Assisted final VictoryConditionsData data,
                             @Assisted final Side side,
                                       final ShipVictoryFactory shipVF) {
        this.side = side;
        shipVictoryFactory = shipVF;

        objectives = data.getObjectives();

        log.info("Build default ship victory conditions for side: {}.", side);
        defaultShips = buildShipConditions(data.getDefaultShip(), createShipVictory);

        log.info("Build scenario ship victory conditions for side: {}.", side);
        scenarioShips = buildShipConditions(data.getScenarioShip(), createShipVictory);

        log.info("Build scenario required ship victory conditions for side: {}.", side);
        requiredShips = buildShipConditions(data.getRequiredShip(), createRequiredShipVictory);

        if (!registered) {
            registerForEvents(defaultShips);
        }

        totalVictoryPoints = data.getTotalVictoryPoints();
    }

    /**
     * Get the data that is read and saved. This is all the data that needs to persist for this class.
     *
     * @return The persistent victory conditions data.
     */
    public VictoryConditionsData getData() {
        VictoryConditionsData data = new VictoryConditionsData();
        data.setObjectives(objectives);
        data.setTotalVictoryPoints(totalVictoryPoints);
        data.setDefaultShip(getVictoryData(defaultShips));
        data.setScenarioShip(getVictoryData(scenarioShips));
        data.setRequiredShip(getVictoryData(requiredShips));

        return data;
    }

    /**
     * Determine if the all the required victory conditions have be met.
     *
     * @return True if all the required victory conditions have been met. False otherwise.
     */
    public boolean requirementsMet() {
        return conditionMet(scenarioShips)
                && conditionMet(requiredShips);
    }

    /**
     * Get the ship victory data.
     *
     * @param shipVictoryConditions The ship victory conditions for which the data is returned.
     * @return The corresponding data of the ship victory conditions is returned.
     */
    private List<ShipVictoryData> getVictoryData(final List<ShipVictoryCondition> shipVictoryConditions) {
        return Optional.ofNullable(shipVictoryConditions)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(ShipVictoryCondition::getData)
                .collect(Collectors.toList());
    }

    /**
     * Build the ship victory conditions.
     *
     * @param data Ship victory conditions read in from a JSON file.
     * @param create The ship victory creation function.
     * @return A list of ship victory conditions.
     */
    private  List<ShipVictoryCondition> buildShipConditions(final List<ShipVictoryData> data,
                                                            final BiFunction<ShipVictoryData, Side, ShipVictoryCondition> create) {
        return Optional.ofNullable(data)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(victoryData -> create.apply(victoryData, side))
                .collect(Collectors.toList());
    }

    /**
     * Register for ship events.
     *
     * @param victoryConditions List of victory conditions.
     */
    private  void registerForEvents(final List<ShipVictoryCondition> victoryConditions) {
        Optional.ofNullable(victoryConditions)
                .ifPresent(conditions -> registerForShipEvents());
    }

    /**
     * Register for ship events.
     */
    private void registerForShipEvents() {
        ShipEvent.register(this, this::handleShipEvent);
        registered = true;
    }

    /**
     * Handle ship events.
     *
     * @param event The ship event.
     */
    private void handleShipEvent(final ShipEvent event) {
        log.info("Handle ship event for ship '{}' {}", event.getShip().getName(), event.getAction());

        //Send the ship event to the required victory conditions.
        checkRequired(event);

        //Get the scenario specific event victory points that were awarded.
        log.info("Check specific scenario victory conditions.");
        Result result = getPoints(scenarioShips, event);

        if (!result.isAwarded()) {
            log.info("No scenario specific victory condition matched. Check default victory conditions.");
            //The event did not trigger any scenario specific victory conditions. Thus,
            //get the points for the default victory conditions.
            result = getPoints(defaultShips, event);
        }

        saveHistory(event, result.getPoints());
    }

    /**
     * Get the victory points for the given ship event.
     *
     * @param victoryConditions List of victory conditions.
     * @param event The fired ship event.
     * @return True if the fired ship event award victory points.
     */
    private Result getPoints(final List<ShipVictoryCondition> victoryConditions, final ShipEvent event) {
        Result result = new Result();
        List<ShipVictoryCondition> matched = Optional.ofNullable(victoryConditions)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(shipVictory -> shipVictory.match(event))
                .collect(Collectors.toList());

        // Some victory conditions may match but not awarded any points.
        result.setAwarded(!matched.isEmpty());

        int points = matched.stream()
                .findFirst()
                .map(shipVictory -> shipVictory.getPoints(event))
                .orElse(0);

        totalVictoryPoints += points;

        result.setPoints(points);
        return result;
    }

    /**
     * Send the ship event to the list of required ship events if it exists.
     *
     * @param event The fired ship event.
     */
    private void checkRequired(final ShipEvent event) {
        Optional.ofNullable(requiredShips)
                .orElseGet(Collections::emptyList)
                .forEach(requiredShipVictory -> requiredShipVictory.match(event));
    }

    /**
     * Save the event and the corresponding victory points awarded into the history.
     *
     * @param event The game event
     * @param points The resulting victory points.
     */
    private void saveHistory(final GameEvent event, final int points) {
        history.add(new History(event, points));
    }

    /**
     * Determine if the victory conditions are all met for a given list of conditions.
     *
     * @param conditions The list of victory conditions.
     * @param <T> The type of the victory conditions.
     * @return True if all of the victory conditions are met. False otherwise.
     */
    private <T extends ShipVictoryCondition> boolean conditionMet(final List<T> conditions) {
        // Ensure that the conditions is not null. It will at least be an empty list.
        return Optional.ofNullable(conditions)
                .orElseGet(Collections::emptyList)
                .stream()
                .allMatch(ShipVictoryCondition::isRequirementMet);
    }
}
