package engima.waratsea.model.victory;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.victory.data.ShipVictoryData;
import engima.waratsea.model.victory.data.VictoryData;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a side's victory conditions for the game.
 */
public class Victory {

    private Side side;

    @Getter
    private int totalVictoryPoints;

    private List<ShipVictory> defaultShips;
    private List<ShipVictory> scenarioShips;

    /**
     * Stores the history of events that award victory points.
     */
    private class History {
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
     */
    public Victory(final VictoryData data, final Side side) {
        this.side = side;
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
        scenarioShips = buildShipConditions(data.getShip());

        if (!registered) {
            registerForEvents(scenarioShips);
        }
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
                .map(victoryData -> new ShipVictory(victoryData, side))
                .collect(Collectors.toList());
    }

    /**
     * Register for ship events.
     * @param victoryConditions List of victory conditions.
     */
    private void registerForEvents(final List<ShipVictory> victoryConditions) {
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
         if (getPoints(scenarioShips, event)) {
             return;
         }

         getPoints(defaultShips, event);
    }

    /**
     * Get the victory points for the given ship event.
     * @param victoryConditions List of victory conditions.
     * @param event The fired ship event.
     * @return True if the fired ship event award victory points.
     */
    private boolean getPoints(final List<ShipVictory> victoryConditions, final ShipEvent event) {
        int points = Optional.ofNullable(victoryConditions)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(shipVictory -> shipVictory.match(event))
                .findFirst()
                .map(shipVictory -> shipVictory.getPoints(event))
                .orElse(0);

        saveHistory(event, points);

        totalVictoryPoints += points;

        return points != 0;
    }

    /**
     * Save the event and the corresponding victory points awarded into the history.
     * @param event The game event
     * @param points The resulting victory points.
     */
    private void saveHistory(final GameEvent event, final int points) {
        history.add(new History(event, points));
    }

}
