package engima.waratsea.model.base.port;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.base.port.data.PortData;

/**
 * Factory used by guice to create ports.
 */
public interface PortFactory {
    /**
     * Creates a port.
     * @param side The side of the port. ALLIES or AXIS.
     * @param data Port data read from a JSON file.
     * @return A port initialized with the data from the JSON file.
     */
    Port create(Side side, PortData data);
}
