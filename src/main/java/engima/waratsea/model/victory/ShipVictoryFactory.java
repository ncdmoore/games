package engima.waratsea.model.victory;

import com.google.inject.name.Named;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.victory.data.ShipVictoryData;

/**
 * Factory used by guice to create ship victory conditions.
 */
public interface ShipVictoryFactory {

    /**
     * Create's the ship victory condition.
     *
     * @param data Ship victory data from a JSON file.
     * @param side The side of the ship victory conditions. ALLIES or AXIS.
     * @return The ship victory object.
     */
    @Named("ship")
    ShipVictoryCondition createShip(ShipVictoryData data, Side side);


    /**
     * Create's the ship victory condition.
     *
     * @param data Ship victory data from a JSON file.
     * @param side The side of the ship victory conditions. ALLIES or AXIS.
     * @return The ship victory object.
     */
    @Named("required")
    ShipVictoryCondition createRequired(ShipVictoryData data, Side side);
}
