package engima.waratsea.model.base.airfield;

import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.game.Side;

/**
 * Factory used by guice to create airfields.
 */
public interface AirfieldFactory {
    /**
     * Creates an airfield.
     * @param side The side of the airfield. ALLIES or AXIS.
     * @param data Airfield data read from a JSON file.
     * @return An airfield initialized with the data from the JSON file.
     */
    Airfield create(Side side, AirfieldData data);
}
