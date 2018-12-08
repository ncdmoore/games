package engima.waratsea.event.turn;

import engima.waratsea.event.turn.TurnEvent;

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
