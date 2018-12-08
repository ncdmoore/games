package engima.waratsea.event.ship;

import engima.waratsea.event.ship.ShipEvent;

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
