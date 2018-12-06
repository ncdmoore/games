package enigma.waratsea.event;

import com.google.inject.Inject;
import engima.waratsea.event.GameEventHandler;
import engima.waratsea.event.GameEventRegistry;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestEventHandlerUnregister implements GameEventHandler<TestEvent> {

    @Inject
    private GameEventRegistry registry;

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

        registry.unregister(TestEvent.class, this);
    }
}
