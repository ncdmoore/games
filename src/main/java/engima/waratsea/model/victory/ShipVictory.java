package engima.waratsea.model.victory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Side;
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

/**
 * Represents a ship victory condition.
 */
@Slf4j
public class ShipVictory implements VictoryCondition {

    // The cargo capacity factor is 3 times the board game value to avoid fractions.
    // Thus the victory for unloading cargo is divided by 3 --> 4 instead of 12.
    private static final int CARGO_CAPACITY_UNLOAD_FACTOR = 4;
    private static final int OUT_OF_FUEL_FACTOR = 2;
    private GameMap gameMap;

    private ShipEventMatcher matcher;

    private int points;         // The points awarded for each occurrence of this victory condition.
    private int totalPoints;    // The total points awarded for all occurrences of this victory condition
    private int requiredPoints; // The total points required for this victory condition to be met.
                                // Note, not all victory conditions have a total point requirement.
                                // In fact most do not have any total point requirement.
    @Getter
    private boolean requirementMet;

    /**
     * Victory calculation function.
     * @param <P1> Parameter one. The default victory points.
     * @param <P2> Paramater two. The fired ship event.
     * @param <R> The calculation result.
     */
    private interface CalculationFunction<P1, P2, R> {
        /**
         * Apply the victory function.
         * @param p1 The default victory points.
         * @param p2 The ship event.
         * @return The calculated victory points.
         */
        R apply(P1 p1, P2 p2);
    }

    private static CalculationFunction<Integer, ShipEvent, Integer> getSunk = (p, e) -> p == 0 ? e.getShip().getVictoryPoints() : p;
    private static CalculationFunction<Integer, ShipEvent, Integer> getOutOfFuel = (p, e) -> e.getShip().getVictoryPoints() / OUT_OF_FUEL_FACTOR;
    private static CalculationFunction<Integer, ShipEvent, Integer> getCargoUnloaded = (p, e) -> p == 0 ? e.getShip().getCargo().getCapacity() * CARGO_CAPACITY_UNLOAD_FACTOR : p;
    private static CalculationFunction<Integer, ShipEvent, Integer> getDefault = (p, e) -> p;

    private static final Map<ShipEventAction, CalculationFunction<Integer, ShipEvent, Integer>> FUNCTION_MAP = new HashMap<>();
    static {
        FUNCTION_MAP.put(ShipEventAction.SUNK, getSunk);
        FUNCTION_MAP.put(ShipEventAction.OUT_OF_FUEL, getOutOfFuel);
        FUNCTION_MAP.put(ShipEventAction.CARGO_UNLOADED, getCargoUnloaded);
    }

    private CalculationFunction<Integer, ShipEvent, Integer> calculation;

    /**
     * Constructor.
     * @param data The victory condition data as read from a JSON file.
     * @param side The side ALLIES or AXIS.
     * @param factory Factory for creating ship event matchers.
     * @param gameMap The game map.
     */
    @Inject
    public ShipVictory(@Assisted final ShipVictoryData data,
                       @Assisted final Side side,
                       final ShipEventMatcherFactory factory,
                       final GameMap gameMap) {

        matcher = factory.create(data.getEvent());
        points = data.getPoints();
        requiredPoints = data.getRequiredPoints();

        // If no required points are specified then this victory condition does not have to be satisfied.
        // Thus, just set the requirement to true. This is necessary as the event which triggers this
        // condition might never be thrown.
        requirementMet = requiredPoints == 0;

        matcher.setSide(side);

        log.info("Victory condition match:");
        matcher.log();
        log.info("Points: {}", points);

        this.gameMap = gameMap;

        calculation = setCalculationFunction();
    }

    /**
     * Determine if a ship event thrown results in a change in victory points.
     * @param event The fired ship event.
     * @return True if the fired ship event is one that resutls in a change in victory points.
     */
    public boolean match(final ShipEvent event) {
         return matcher.match(event);
    }

    /**
     * Determine the victory points.
     * @param event The fired ship event.
     * @return The number of victory points award due to the fired ship event.
     */
    public int getPoints(final ShipEvent event) {
        int awardedPoints = calculation.apply(points, event);
        totalPoints += awardedPoints;
        requirementMet = totalPoints >= requiredPoints;

        String location = gameMap.convertReferenceToName(event.getShip().getTaskForce().getLocation());
        log.info("Ship: '{}' '{}', location: '{}'. Victory points awarded: {}", new Object[] {event.getShip().getName(),
                event.getAction(), location, awardedPoints});

        return awardedPoints;
    }

    /**
     * Determine the function to calculate victory points.
     * @return The victory point calculation function.
     */
    private CalculationFunction<Integer, ShipEvent, Integer> setCalculationFunction() {
        return  FUNCTION_MAP.getOrDefault(ShipEventAction.valueOf(matcher.getAction()), getDefault);
    }
}
