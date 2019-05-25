package engima.waratsea.model.flotilla;

import engima.waratsea.model.flotilla.data.FlotillaData;
import engima.waratsea.model.game.Side;

/**
 * Factory used by guice to create flotillas.
 */
public interface FlotillaFactory {
    /**
     * Creates a Flotilla.
     *
     * @param side The side of the Flotilla. ALLIES or AXIS.
     * @param data Flotilla data read from a JSON file.
     * @return A Flotilla initialized with the data from the JSON file.
     */
    Flotilla create(Side side, FlotillaData data);
}
