package enigma.waratsea.event;

import engima.waratsea.event.GameEventHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestEventHandler implements GameEventHandler<TestEvent> {

    @Getter
    @Setter
    private boolean eventReceived = false;

    /**
     * This method is called to notify the event handler that an event has fired.
     *
     * @param testEvent the fired event.
     */
    @Override
    public void notify(TestEvent testEvent) {
        log.info("Test event recievied: {} by {}", testEvent.getAction(), this);
        eventReceived = true;
    }
}
