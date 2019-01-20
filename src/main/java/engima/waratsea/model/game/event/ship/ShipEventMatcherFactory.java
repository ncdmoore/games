package engima.waratsea.model.game.event.ship;


import engima.waratsea.model.game.event.ship.data.ShipMatchData;

/**
 * Factory used by guice to create ship event matchers.
 */
public interface ShipEventMatcherFactory {
    /**
     * Creates a ship event matcher.
     * @param data ship event matcher data read from a JSON file.
     * @return A ship event matcher initialized with the data from the JSON file.
     */
    ShipEventMatcher create(ShipMatchData data);
}
