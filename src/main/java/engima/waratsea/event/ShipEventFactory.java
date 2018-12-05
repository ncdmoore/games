package engima.waratsea.event;

/**
 * Ship event factory used by guice.
 */
public interface ShipEventFactory {

    /**
     * Create a ship event.
     * @return A ship event.
     */
     ShipEvent create();
}
