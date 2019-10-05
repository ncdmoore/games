package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventAction;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.ShipEventMatcherFactory;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.victory.data.ShipVictoryData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Represents a ship victory condition.
 */
@Slf4j
public class ShipVictory implements VictoryCondition<ShipEvent, ShipVictoryData> {
    // The cargo capacity factor is 3 times the board game value to avoid fractions.
    // Thus the victory for unloading cargo is divided by 3.
    // The game manual specifies a victory factor of 12 under Victory Points.
    // This now becomes 4.   (12 / 3 = 4)
    private static final int CARGO_CAPACITY_UNLOAD_FACTOR = 4;
    private static final int OUT_OF_FUEL_FACTOR = 2;
    private GameMap gameMap;

    private ShipEventMatcher matcher;

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

    private static BiFunction<Integer, ShipEvent, Integer> getShip = (p, e) -> p == 0 ? e.getShip().getVictoryPoints() : p;
    private static BiFunction<Integer, ShipEvent, Integer> getOutOfFuel = (p, e) -> e.getShip().getVictoryPoints() / OUT_OF_FUEL_FACTOR;
    private static BiFunction<Integer, ShipEvent, Integer> getCargoUnloaded = (p, e) -> p == 0 ? e.getShip().getCargo().getCapacity() * CARGO_CAPACITY_UNLOAD_FACTOR : p;
    private static BiFunction<Integer, ShipEvent, Integer> getDefault = (p, e) -> p;

    private static final Map<ShipEventAction, BiFunction<Integer, ShipEvent, Integer>> FUNCTION_MAP = new HashMap<>();
    static {
        FUNCTION_MAP.put(ShipEventAction.SUNK, getShip);
        FUNCTION_MAP.put(ShipEventAction.OUT_OF_FUEL, getOutOfFuel);
        FUNCTION_MAP.put(ShipEventAction.CARGO_UNLOADED, getCargoUnloaded);
        FUNCTION_MAP.put(ShipEventAction.ARRIVAL, getShip);
    }

    private BiFunction<Integer, ShipEvent, Integer> calculation;

    /**
     * Constructor.
     *
     * @param data The victory condition data as read from a JSON file.
     * @param factory Factory for creating ship event matchers.
     * @param gameMap The game map.
     */
    @Inject
    public ShipVictory(@Assisted final ShipVictoryData data,
                       final ShipEventMatcherFactory factory,
                       final GameMap gameMap) {

        matcher = factory.create(data.getEvent());
        points = data.getPoints();
        totalPoints = data.getTotalPoints();
        requiredPoints = data.getRequiredPoints();
        requiredOccurrences = data.getRequiredOccurrences();
        occurrenceCount = data.getOccurrenceCount();
        requirementMet = data.isRequirementMet();

        log.debug("Ship victory condition match:");
        matcher.log();
        log.debug("Points: {}", points);

        this.gameMap = gameMap;

        calculation = setCalculationFunction();

        // This is needed if no events that trigger this condition are thrown.
        // We must initialize the requirementMet.
        requirementMet = totalPoints >= requiredPoints;
    }

    /**
     * Get the corresponding ship victory data. This is the data that is read and written. This is all the data that
     * needs to persist for this class.
     *
     * @return The corresponding persistent ship victory data.
     */
    @Override
    public ShipVictoryData getData() {
        ShipVictoryData data = new ShipVictoryData();
        data.setEvent(matcher.getData());
        data.setPoints(points);
        data.setTotalPoints(totalPoints);
        data.setRequiredPoints(requiredPoints);
        data.setRequiredOccurrences(requiredOccurrences);
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

    /**
     * Determine if a ship event thrown results in a change in victory points.
     *
     * @param event The fired ship event.
     * @return True if the fired ship event is one that results in a change in victory points.
     */
    public boolean match(final ShipEvent event) {
        String location = gameMap.convertPortReferenceToName(event.getShip().getTaskForce().getReference());

        boolean matched = matcher.match(event);

        log.info("Ship: '{}' '{}', reference: '{}'. matched: {}", new Object[]{event.getShip().getName(),
                event.getAction(), location, matched});

        return matched;
    }

    /**
     * Determine the victory points.
     *
     * @param event The fired ship event.
     * @return The number of victory points award due to the fired ship event.
     */
    public int getPoints(final ShipEvent event) {
        int awardedPoints = 0;

        String location = gameMap.convertPortReferenceToName(event.getShip().getTaskForce().getReference());

        if (occurrencesMet()) {
            awardedPoints = calculation.apply(points, event);
            totalPoints += awardedPoints;
            requirementMet = totalPoints >= requiredPoints;
        }

        log.info("Ship: '{}' '{}', reference: '{}'. Occurrence: {}, Required: {}. Victory points awarded: {}", new Object[]{event.getShip().getName(),
                event.getAction(), location, occurrenceCount, requiredOccurrences, awardedPoints});

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
     * Determine the function to calculate victory points.
     *
     * @return The victory point calculation function.
     */
    private BiFunction<Integer, ShipEvent, Integer> setCalculationFunction() {
        return  FUNCTION_MAP.getOrDefault(ShipEventAction.valueOf(matcher.getAction()), getDefault);
    }
}
