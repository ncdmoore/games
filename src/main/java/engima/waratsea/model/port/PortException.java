package engima.waratsea.model.port;

import lombok.extern.slf4j.Slf4j;

/**
 * This is for exceptions that happen while attempting to read the port JSON file.
 */
@Slf4j
public class PortException extends Exception {
    /**
     *
     * This exception is thrown when an exception occurs during the creation of a port.
     *
     * @param description   The description of the exception.
     *
     */
    public PortException(final String description) {
        super(description);
        log.error(description);
    }

}
