package engima.waratsea.model.game.event.airfield;


import engima.waratsea.model.game.event.airfield.data.AirfieldMatchData;

/**
 * Factory used by guice to create airfield event matchers.
 */
public interface AirfieldEventMatcherFactory {
    /**
     * Creates an airfield event matcher.
     * @param data airfield event matcher data read from a JSON file.
     * @return A airfield event matcher initialized with the data from the JSON file.
     */
    AirfieldEventMatcher create(AirfieldMatchData data);
}
