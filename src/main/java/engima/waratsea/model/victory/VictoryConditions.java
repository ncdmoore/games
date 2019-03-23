package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.game.event.airfield.AirfieldEvent;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.squadron.SquadronEvent;
import engima.waratsea.model.victory.data.AirfieldVictoryData;
import engima.waratsea.model.victory.data.ShipVictoryData;
import engima.waratsea.model.victory.data.SquadronVictoryData;
import engima.waratsea.model.victory.data.VictoryConditionsData;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a side's victory conditions for the game.
 * Thus, there are only two instances of this class constructed.
 * One for the ALLIES and one for the AXIS.
 */
@Slf4j
public class VictoryConditions {

    @Getter
    private int totalVictoryPoints;

    @Getter
    private String objectives;

    private List<VictoryCondition<ShipEvent, ShipVictoryData>> defaultShips;              // The default victory conditions for ship.
    private List<VictoryCondition<ShipEvent, ShipVictoryData>> scenarioShips;             // Scenario specific victory conditions for ships. These may override the default.
    private List<VictoryCondition<ShipEvent, ShipVictoryData>> requiredShips;             // Scenario required ship victory conditions.

    private List<VictoryCondition<SquadronEvent, SquadronVictoryData>> defaultSquadron;   // The default victory condition for squadrons.
    private List<VictoryCondition<SquadronEvent, SquadronVictoryData>> scenarioSquadron;  // Scenario specific victory condition for squadrons.

    private List<VictoryCondition<AirfieldEvent, AirfieldVictoryData>> scenarioAirfields; // Scenario specific victory condition for airfields.

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

    @Getter
    private List<History> history = new ArrayList<>();

    /**
     * Constructor of the a side's victory.
     *
     * @param data The victory data read in from a JSON file.
     * @param side The side ALLIES or AXIS of the victory conditions.
     * @param shipVictoryFactory Ship victory factory.
     * @param airfieldVictoryFactory Airfield victory factory.
     * @param squadronVictoryFactory Squadron victory factory.
     */
    @Inject
    public VictoryConditions(@Assisted final VictoryConditionsData data,
                             @Assisted final Side side,
                             final ShipVictoryFactory<ShipEvent, ShipVictoryData> shipVictoryFactory,
                             final AirfieldVictoryFactory<AirfieldEvent, AirfieldVictoryData> airfieldVictoryFactory,
                             final SquadronVictoryFactory<SquadronEvent, SquadronVictoryData> squadronVictoryFactory) {
        objectives = data.getObjectives();

        log.info("Build default ship victory conditions for side: {}.", side);
        defaultShips = buildConditions(data.getDefaultShip(), shipVictoryFactory::createShip);

        log.info("Build scenario ship victory conditions for side: {}.", side);
        scenarioShips = buildConditions(data.getScenarioShip(), shipVictoryFactory::createShip);

        log.info("Build scenario required ship victory conditions for side: {}.", side);
        requiredShips = buildConditions(data.getRequiredShip(), shipVictoryFactory::createRequired);

        log.info("Build default squadron victory conditions for side: {}.", side);
        defaultSquadron = buildConditions(data.getDefaultSquadron(), squadronVictoryFactory::create);

        log.info("Build scenario squadron victory conditions for side: {}.", side);
        scenarioSquadron = buildConditions(data.getScenarioSquadron(), squadronVictoryFactory::create);

        log.info("Build scenario airfield victory conditions for side: {}.", side);
        scenarioAirfields = buildConditions(data.getScenarioAirfield(), airfieldVictoryFactory::createAirfield);

        registerForEvents();

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
        data.setDefaultShip(PersistentUtility.getData(defaultShips));
        data.setScenarioShip(PersistentUtility.getData(scenarioShips));
        data.setRequiredShip(PersistentUtility.getData(requiredShips));
        data.setDefaultSquadron(PersistentUtility.getData(defaultSquadron));
        data.setScenarioSquadron(PersistentUtility.getData(scenarioSquadron));
        data.setScenarioAirfield(PersistentUtility.getData(scenarioAirfields));

        return data;
    }

    /**
     * Determine if the all the required victory conditions have be met.
     *
     * @return True if all the required victory conditions have been met. False otherwise.
     */
    public boolean requirementsMet() {
        return conditionMet(scenarioShips)
                && conditionMet(requiredShips)
                && conditionMet(scenarioAirfields);
    }

    /**
     * Build the ship victory conditions.
     *
     * @param <E> The event type.
     * @param <D> The victory condition data type.
     * @param data Victory conditions read in from a JSON file.
     * @param create The victory creation function.
     * @return A list of victory conditions.
     */
    private  <E, D> List<VictoryCondition<E, D>> buildConditions(final List<D> data,
                                                                 final Function<D, VictoryCondition<E, D>> create) {
        return Optional.ofNullable(data)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(create)
                .collect(Collectors.toList());
    }

    /**
     * Register for events.
     */
    private void registerForEvents() {
        ShipEvent.register(this, this::handleShipEvent);
        SquadronEvent.register(this, this::handleSquadronEvent);
        AirfieldEvent.register(this, this::handleAirfieldEvent);
    }

    /**
     * Handle ship events.
     *
     * @param event The ship event.
     */
    private void handleShipEvent(final ShipEvent event) {
        log.info("Handle ship event for ship '{}' {}", event.getShip().getName(), event.getAction());

        //Send the ship event to the required victory conditions.
        checkRequired(requiredShips, event);

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
     * Handle squadron events.
     *
     * @param event The squadron event.
     */
    private void handleSquadronEvent(final SquadronEvent event) {
        log.info("Handle airfield event for airfield '{}' {}", event.getSquadron().getName(), event.getAction());

        //Get the scenario specific event victory points that were awarded.
        log.info("Check specific scenario victory conditions.");
        Result result = getPoints(scenarioSquadron, event);

        if (!result.isAwarded()) {
            log.info("No scenario specific victory condition matched. Check default victory conditions.");
            //The event did not trigger any scenario specific victory conditions. Thus,
            //get the points for the default victory conditions.
            result = getPoints(defaultSquadron, event);
        }

        saveHistory(event, result.getPoints());
    }

    /**
     * Handle airfield events.
     *
     * @param event The airfield event.
     */
    private void handleAirfieldEvent(final AirfieldEvent event) {
        log.info("Handle airfield event for airfield '{}' {}", event.getAirfield().getName(), event.getAction());
        Result result = getPoints(scenarioAirfields, event);
        saveHistory(event, result.getPoints());
    }

    /**
     * Get the victory points for the given event.
     *
     * @param <E> The corresponding event type.
     * @param <D> The JSON data type.
     * @param victoryConditions List of victory conditions.
     * @param event The fired event.
     * @return True if the fired event award victory points.
     */
    private <E, D> Result getPoints(final List<VictoryCondition<E, D>> victoryConditions, final E event) {
        Result result = new Result();
        List<VictoryCondition<E, D>> matched = Optional.ofNullable(victoryConditions)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(condition -> condition.match(event))
                .collect(Collectors.toList());

        // Some victory conditions may match but not awarded any points.
        // This happens when multiple occurrences of an event is needed before points are awarded.
        // The event will match, but no points are awarded.
        result.setAwarded(!matched.isEmpty());

        int points = matched.stream()
                .findFirst()
                .map(condition -> condition.getPoints(event))
                .orElse(0);

        totalVictoryPoints += points;

        result.setPoints(points);
        return result;
    }

    /**
     * Send the event to the list of required events if it exists.
     *
     * @param <E> The corresponding event type.
     * @param <D> The JSON data type.
     * @param victoryConditions  List of victory conditions.
     * @param event The fired event.
     */
    private <E, D> void checkRequired(final List<VictoryCondition<E, D>> victoryConditions, final E event) {
        Optional.ofNullable(victoryConditions)
                .orElseGet(Collections::emptyList)
                .forEach(condition -> condition.match(event));
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
    private <T extends VictoryCondition> boolean conditionMet(final List<T> conditions) {
        // Ensure that the conditions is not null. It will at least be an empty list.
        return Optional.ofNullable(conditions)
                .orElseGet(Collections::emptyList)
                .stream()
                .allMatch(VictoryCondition::isRequirementMet);
    }
}
