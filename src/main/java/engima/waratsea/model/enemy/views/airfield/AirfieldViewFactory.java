package engima.waratsea.model.enemy.views.airfield;

import engima.waratsea.model.enemy.views.airfield.data.AirfieldViewData;

/**
 * Factory used by guice to create ports.
 */
public interface AirfieldViewFactory {
    /**
     * Creates an airfield.
     * @param data Airfield data read from a JSON file.
     * @return A airfield initialized with the data from the JSON file.
     */
    AirfieldView create(AirfieldViewData data);
}
