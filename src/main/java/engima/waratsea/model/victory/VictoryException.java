package engima.waratsea.model.victory;

import lombok.extern.slf4j.Slf4j;

/**
 * This exception is thrown when the game victory cannot be loaded properly.
 */
@Slf4j
public class VictoryException extends Exception {
    /**
     *
     * This exception is thrown when an exception occurs during the creation of the game victory.
     *
     * @param description   The description of the exception.
     *
     */
    public VictoryException(final String description) {
        super(description);
        log.error(description);
    }

}
