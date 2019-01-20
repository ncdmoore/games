package engima.waratsea.model.map;

import lombok.extern.slf4j.Slf4j;

/**
 * This exception is thrown when the map region file cannot be loaded properly.
 */
@Slf4j
public class MapException extends Exception {
    /**
     *
     * This exception is thrown when an exception occurs during the creation of a map regions.
     *
     * @param description   The description of the exception.
     *
     */
    public MapException(final String description) {
        super(description);
        log.error(description);
    }

}
