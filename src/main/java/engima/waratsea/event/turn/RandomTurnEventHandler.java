package engima.waratsea.event.turn;

/**
 * Implement this interface to receive random turn events.
 */
public interface RandomTurnEventHandler {

    /**
     * Notify of a random turn event.
     *
     * @param randomTurnEvent The random turn event.
     */
    void notify(RandomTurnEvent randomTurnEvent);
}
