package engima.waratsea.model.ship;

import lombok.extern.slf4j.Slf4j;

/**
 * This is for exceptions that happen while attempting to read the ship class JSON file.
 */
@Slf4j
public class ShipyardException extends Exception {
    /**
     *
     * This exception is thrown when an exception occurs during the creation of a ship.
     *
     * @param description   The description of the exception.
     *
     */
    public ShipyardException(final String description) {
        super(description);
        log.error(description);
    }

}
