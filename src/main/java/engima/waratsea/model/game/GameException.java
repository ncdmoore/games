package engima.waratsea.model.game;

import lombok.extern.slf4j.Slf4j;

/**
 * This exception is thrown when a game cannot be loaded properly.
 */
@Slf4j
public class GameException extends Exception {
    /**
     *
     * This exception is thrown when an exception occurs during the creation of a game.
     *
     * @param description   The description of the exception.
     *
     */
    public GameException(final String description) {
        super(description);
        log.error(description);
    }

}
