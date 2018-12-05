package enigma.waratsea.event;

import engima.waratsea.event.ShipEvent;

/**
 * Test event factory used by guice.
 */
public interface TestEventFactory {

    /**
     * Create a test event.
     * @return A test event.
     */
     TestEvent create();
}
