package engima.waratsea.model.scenario;

import lombok.extern.slf4j.Slf4j;

/**
 * This exception is thrown when a game scenario cannot be loaded properly.
 */
@Slf4j
public class ScenarioException extends Exception {
    /**
     *
     * This exception is thrown when an exception occurs during the creation of a game scenario.
     *
     * @param description   The description of the exception.
     *
     */
    public ScenarioException(final String description) {
        super(description);
        log.error(description);
    }

}
