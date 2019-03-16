package engima.waratsea.model.map;

/**
 * Factory used by guice to create locations.
 */
public interface LocationFactory {
    /**
     * Creates a location.
     *
     * @param name The name of the location.
     * @return A location object.
     */
    Location create(String name);
}
