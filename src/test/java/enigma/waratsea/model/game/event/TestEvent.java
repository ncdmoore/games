package enigma.waratsea.model.game.event;

import engima.waratsea.model.game.event.GameEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestEvent extends GameEvent {
    private static List<TestEventHandler> handlers = new ArrayList<>();

    /**
     * Initialize the test event class. This method clears out all test event handlers.
     */
    public static void init() {
        handlers.clear();
    }

    /**
     * This is how event handlers register to receive test event notifications.
     *
     * @param testEventHandler The test event handler that is registered.
     */
    public static void register(final TestEventHandler testEventHandler) {
        handlers = add(TestEvent.class, handlers, testEventHandler);
    }

    /**
     * This is how event handlers unregister for test event notifications.
     *
     * @param testEventHandler The test event handler that is unregistered.
     */
    public static void unregister(final TestEventHandler testEventHandler) {
        handlers = remove(TestEvent.class, handlers, testEventHandler);
    }


    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    public void fire() {
        log.info("Fire test event: {}", action);
        handlers.forEach(h -> h.notify(this));
    }

    @Getter
    @Setter
    private String action;

    @Getter
    @Setter
    private String name;

    public TestEvent() {
        action = "Test";
    }
}
