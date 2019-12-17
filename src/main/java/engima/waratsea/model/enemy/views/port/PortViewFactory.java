package engima.waratsea.model.enemy.views.port;

import engima.waratsea.model.enemy.views.port.data.PortViewData;

/**
 * Factory used by guice to create ports.
 */
public interface PortViewFactory {
    /**
     * Creates a port.
     * @param data Port data read from a JSON file.
     * @return A port initialized with the data from the JSON file.
     */
    PortView create(PortViewData data);
}
