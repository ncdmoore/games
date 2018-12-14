package engima.waratsea.event.turn;

/**
 * Implement this interface to receive turn events.
 */
public interface TurnEventHandler {
    /**
     * Notify of a turn event.
     * @param turnEvent The turn event.
     */
    void notify(TurnEvent turnEvent);
}
