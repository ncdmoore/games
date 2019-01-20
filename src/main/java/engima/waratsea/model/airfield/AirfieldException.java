package engima.waratsea.model.airfield;

import lombok.extern.slf4j.Slf4j;

/**
 * This is for exceptions that happen while attempting to read the airfield JSON file.
 */
@Slf4j
public class AirfieldException extends Exception {
    /**
     *
     * This exception is thrown when an exception occurs during the creation of an airfield.
     *
     * @param description   The description of the exception.
     *
     */
    public AirfieldException(final String description) {
        super(description);
        log.error(description);
    }

}
