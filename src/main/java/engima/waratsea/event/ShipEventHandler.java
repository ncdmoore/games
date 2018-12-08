package engima.waratsea.event;

/**
 * Implement this interface to receive ship events.
 */
public interface ShipEventHandler {

    /**
     * Notify of a ship event.
     *
     * @param shipEvent The ship event.
     */
    void notify(ShipEvent shipEvent);
}
