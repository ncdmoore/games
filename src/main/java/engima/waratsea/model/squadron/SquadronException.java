package engima.waratsea.model.squadron;

import lombok.extern.slf4j.Slf4j;

/**
 * This is for exceptions that happen while attempting to read the squadron JSON files.
 */
@Slf4j
public class SquadronException extends Exception {
    /**
     *
     * This exception is thrown when an exception occurs during the creation of a squadron.
     *
     * @param description The description of the exception.
     *
     */
    public SquadronException(final String description) {
        super(description);
        log.error(description);
    }

}
