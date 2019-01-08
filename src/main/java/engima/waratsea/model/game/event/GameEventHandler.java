package engima.waratsea.model.game.event;

/**
 * Implement this interface to receive game events.
 *
 * @param <E> The type of game event to receive.
 */
public interface GameEventHandler<E> {

    /**
     * Notify the handler that the event occurred.
     *
     * @param event The event.
     */
    void notify(E event);
}
