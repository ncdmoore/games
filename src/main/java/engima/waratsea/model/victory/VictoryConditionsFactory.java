package engima.waratsea.model.victory;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.victory.data.VictoryConditionsData;

/**
 * Factory used by guice to create victory conditions.
 */
public interface VictoryConditionsFactory {

    /**
     * Create's the victory.
     *
     * @param data Victory data from a JSON file.
     * @param side The side of the victory conditions. ALLIES or AXIS.
     * @return The Victory object.
     */
    VictoryConditions create(VictoryConditionsData data, Side side);
}
