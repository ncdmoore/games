package engima.waratsea.model.victory;

import com.google.inject.name.Named;
import engima.waratsea.model.game.Side;

/**
 * Creates airfield victory conditions.
 *
 * @param <E> The type of game event.
 * @param <D> The type of victory data.
 */
public interface AirfieldVictoryFactory<E, D> {
    /**
     * Create's the ship victory condition.
     *
     * @param data Ship victory data from a JSON file.
     * @param side The side of the ship victory conditions. ALLIES or AXIS.
     * @return The ship victory object.
     */
    @Named("airfield")
    VictoryCondition<E, D> createAirfield(D data, Side side);
}
