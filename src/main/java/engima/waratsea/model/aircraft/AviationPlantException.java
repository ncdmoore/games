package engima.waratsea.model.aircraft;

import lombok.extern.slf4j.Slf4j;

/**
 * This is for exceptions that happen while attempting to read the aircraft model JSON file.
 */
@Slf4j
public class AviationPlantException extends Exception {
    /**
     *
     * This exception is thrown when an exception occurs during the creation of an aircraft model.
     *
     * @param description   The description of the exception.
     *
     */
    public AviationPlantException(final String description) {
        super(description);
        log.error(description);
    }

}
