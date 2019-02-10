package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.victory.data.RequiredShipVictoryData;
import engima.waratsea.model.victory.data.ShipVictoryData;
import engima.waratsea.model.victory.data.VictoryData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a side's victory conditions for the game.
 */
@Slf4j
public class VictoryConditions {

    private Side side;

    @Getter
    private int totalVictoryPoints;

    @Getter
    private String objectives;

    private List<ShipVictory> defaultShips;
    private List<ShipVictory> scenarioShips;
    private List<RequiredShipVictory> requiredShips;

    private ShipVictoryFactory shipVictoryFactory;
    private RequiredShipVictoryFactory requiredShipVictoryFactory;

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
     * @param data The victory data read in from a JSON file.
     * @param side The side ALLIES or AXIS of the victory conditions.
     * @param shipVictoryFactory Ship victory factory.
     * @param requiredShipVictoryFactory Required ship victory factory.
     */
    @Inject
    public VictoryConditions(@Assisted final VictoryData data,
                             @Assisted final Side side,
                                       final ShipVictoryFactory shipVictoryFactory,
                                       final RequiredShipVictoryFactory requiredShipVictoryFactory) {
        this.side = side;
        this.shipVictoryFactory = shipVictoryFactory;
        this.requiredShipVictoryFactory = requiredShipVictoryFactory;

        log.info("Build default victory conditions.");
        defaultShips = buildShipConditions(data.getShip());

        if (!registered) {
            registerForEvents(defaultShips);
        }
    }

    /**
     * Add the scenario specific victory conditions.
     * @param data The victory data for a specific scenario read in from a JSON file.
     */
    public void addScenarioConditions(final VictoryData data) {
        log.info("Add scenario specific victory conditions.");
        objectives = data.getObjectives();
        scenarioShips = buildShipConditions(data.getShip());
        requiredShips = buildRequiredShipConditions(data.getRequiredShip());

        if (!registered) {
            registerForEvents(scenarioShips);
        }
    }

    /**
     * Determine if the all the required victory conditions have be met.
     * @return True if all the required victory conditions have been met. False otherwise.
     */
    public boolean requirementsMet() {
        return conditionMet(scenarioShips)
                && conditionMet(requiredShips);
    }

    /**
     * Build the ship victory conditions.
     * @param data Ship victory conditions read in from a JSON file.
     * @return A list of ship victory conditions.
     */
    private List<ShipVictory> buildShipConditions(final List<ShipVictoryData> data) {
        return Optional.ofNullable(data)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(victoryData -> shipVictoryFactory.create(victoryData, side))
                .collect(Collectors.toList());
    }

    /**
     * Build the required ship victory conditions.
     * @param data Required ship victory conditions read in from a JSON file.
     * @return A list of required ship victory conditions.
     */
    private List<RequiredShipVictory> buildRequiredShipConditions(final List<RequiredShipVictoryData> data) {
        return Optional.ofNullable(data)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(victoryData -> requiredShipVictoryFactory.create(victoryData, side))
                .collect(Collectors.toList());
    }

    /**
     * Register for ship events.
     * @param victoryConditions List of victory conditions.
     * @param <T> The type of the victory conditions.
     */
    private <T> void registerForEvents(final List<T> victoryConditions) {
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
     * @param event The ship event.
     */
    private void handleShipEvent(final ShipEvent event) {
        log.info("Handle ship event for ship '{}' {}", event.getShip().getName(), event.getAction());

        //Send the ship event to the required victory conditions.
        checkRequired(event);

        //Get the scenario specific event victory points that were awarded.
        int points = getPoints(scenarioShips, event);

        if (points == 0) {
            //The event did not trigger any scenario specific victory conditions. Thus,
            //get the points for the default victory conditions.
            points = getPoints(defaultShips, event);
        }

        saveHistory(event, points);
    }

    /**
     * Get the victory points for the given ship event.
     * @param victoryConditions List of victory conditions.
     * @param event The fired ship event.
     * @return True if the fired ship event award victory points.
     */
    private int getPoints(final List<ShipVictory> victoryConditions, final ShipEvent event) {
        int points = Optional.ofNullable(victoryConditions)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(shipVictory -> shipVictory.match(event))
                .findFirst()
                .map(shipVictory -> shipVictory.getPoints(event))
                .orElse(0);

        totalVictoryPoints += points;

        return points;
    }

    /**
     * Send the ship event to the list of required ship events if it exists.
     * @param event The fired ship event.
     */
    private void checkRequired(final ShipEvent event) {
        Optional.ofNullable(requiredShips)
                .orElseGet(Collections::emptyList)
                .forEach(requiredShipVictory -> requiredShipVictory.match(event));
    }

    /**
     * Save the event and the corresponding victory points awarded into the history.
     * @param event The game event
     * @param points The resulting victory points.
     */
    private void saveHistory(final GameEvent event, final int points) {
        history.add(new History(event, points));
    }

    /**
     * Determine if the victory conditions are all met for a given list of conditions.
     * @param conditions The list of victory conditions.
     * @param <T> The type of the victory conditions.
     * @return True if all of the victory conditions are met. False otherwise.
     */
    private <T extends VictoryCondition> boolean conditionMet(final List<T> conditions) {
        // Ensure that the conditions is not null. It will at least be an empty list.
        return Optional.ofNullable(conditions)
                .orElseGet(Collections::emptyList)
                .stream()
                .allMatch(VictoryCondition::isRequirementMet);
    }
}
