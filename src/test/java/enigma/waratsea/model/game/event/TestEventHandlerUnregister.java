package enigma.waratsea.model.game.event;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestEventHandlerUnregister  {

    @Getter
    @Setter
    private boolean eventReceived = false;

    public void register() {
        TestEvent.register(this, this::notify);
    }

    /**
     * This method is called to notify the event handler that an event has fired.
     *
     * @param testEvent the fired event.
     */
    private void notify(TestEvent testEvent) {
        log.info("Test event received: {} by {}", testEvent.getAction(), this);
        eventReceived = true;

        TestEvent.unregister(this);
    }
}
