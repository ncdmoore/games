package engima.waratsea.model.base.port;

import engima.waratsea.model.base.port.data.PortData;

/**
 * Factory used by guice to create ports.
 */
public interface PortFactory {
    /**
     * Creates a port.
     * @param data Port data read from a JSON file.
     * @return A port initialized with the data from the JSON file.
     */
    Port create(PortData data);
}
