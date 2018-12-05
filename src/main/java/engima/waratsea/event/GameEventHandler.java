package engima.waratsea.event;

/**
 * Defines event handler interface. Implement this interface to be able to receive and process game events.
 * @param <E> The Event class that the implementation will process.
 */
public interface GameEventHandler<E> {

    /**
     * This method is called to notify the event handler that an event has fired.
     * @param e the fired event.
     */
    void notify(E e);
}
