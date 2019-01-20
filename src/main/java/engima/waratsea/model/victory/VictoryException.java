package engima.waratsea.model.victory;

import lombok.extern.slf4j.Slf4j;

/**
 * This exception is thrown when the game victory cannot be loaded properly.
 */
@Slf4j
public class VictoryException extends Exception {
    /**
     * This exception is thrown when an exception occurs during the creation of the game victory.
     * @param description The description of the exception.
     *
     */
    public VictoryException(final String description) {
        super(description);
        log.error(description);
    }

    /**
     * This exception is thrown when an exception occurs during the creation of the game victory.
     * @param description The description of the exception.
     * @param warn This is not an error but should be warned. This is just a dummy variable to get a different
     *             constructor signature.
     */
    public VictoryException(final String description, final String warn) {
        super(description);
        log.warn(description);
    }

}
