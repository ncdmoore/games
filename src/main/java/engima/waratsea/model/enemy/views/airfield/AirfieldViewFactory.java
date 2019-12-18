package engima.waratsea.model.enemy.views.airfield;

import engima.waratsea.model.enemy.views.airfield.data.AirfieldViewData;

/**
 * Factory used by guice to create ports.
 */
public interface AirfieldViewFactory {
    /**
     * Creates an airfield view.
     * @param data airfield view data read from a JSON file.
     * @return A airfield view initialized with the data from the JSON file.
     */
    AirfieldView create(AirfieldViewData data);
}
