package engima.waratsea.model.flotilla;

import com.google.inject.name.Named;
import engima.waratsea.model.flotilla.data.FlotillaData;
import engima.waratsea.model.game.Side;

/**
 * Factory used by guice to create flotillas.
 */
public interface FlotillaFactory {
    /**
     * Creates a Submarine flotilla.
     *
     * @param side The side of the Flotilla. ALLIES or AXIS.
     * @param data Flotilla data read from a JSON file.
     * @return A Flotilla initialized with the data from the JSON file.
     */
    @Named("submarine")
    Flotilla createSubmarineFlotilla(Side side, FlotillaData data);

    /**
     * Create a MTB flotilla.
     *
     * @param side The side of the Flotilla. ALLIES or AXIS.
     * @param data Flotilla data read from a JSON file.
     * @return A Flotilla initialized with the data from the JSON file.
     */
    @Named("mtb")
    Flotilla createMTBFlotilla(Side side, FlotillaData data);
}
