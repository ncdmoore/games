package engima.waratsea.model.enemy.views.port;

import engima.waratsea.model.enemy.views.port.data.PortViewData;

/**
 * Factory used by guice to create ports.
 */
public interface PortViewFactory {
    /**
     * Creates a port view.
     * @param data port view data read from a JSON file.
     * @return A port view initialized with the data from the JSON file.
     */
    PortView create(PortViewData data);
}
