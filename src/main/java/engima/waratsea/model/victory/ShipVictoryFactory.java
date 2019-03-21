package engima.waratsea.model.victory;

import com.google.inject.name.Named;
import engima.waratsea.model.game.Side;

/**
 * Factory used by guice to create ship victory conditions.
 *
 * @param <E> The type of game event.
 * @param <D> The type of victory data.
 */
public interface ShipVictoryFactory<E, D> {

    /**
     * Create's the ship victory condition.
     *
     * @param data Ship victory data from a JSON file.
     * @param side The side of the ship victory conditions. ALLIES or AXIS.
     * @return The ship victory object.
     */
    @Named("ship")
    VictoryCondition<E, D> createShip(D data, Side side);


    /**
     * Create's the ship victory condition.
     *
     * @param data Ship victory data from a JSON file.
     * @param side The side of the ship victory conditions. ALLIES or AXIS.
     * @return The ship victory object.
     */
    @Named("required")
    VictoryCondition<E, D> createRequired(D data, Side side);

}
