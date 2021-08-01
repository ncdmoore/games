package enigma.waratsea.model.game.event;

import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.game.event.GameEventDispatcher;
import engima.waratsea.model.game.event.GameEventHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestEvent extends GameEvent {
    private static final GameEventDispatcher<TestEvent> DISPATCHER = new GameEventDispatcher<>();

    /**
     * Initialize the test event class. This method clears out all test event handlers.
     */
    public static void init() {
        DISPATCHER.clear();
    }

    /**
     * This is how event handlers register to receive test event notifications.
     *
     * @param key The object registering
     * @param testEventHandler The test event handler that is registered.
     */
    public static void register(final Object key, final GameEventHandler<TestEvent> testEventHandler) {
        DISPATCHER.register(key, testEventHandler);
    }

    /**
     * This is how event handlers unregister for test event notifications.
     *
     * @param key The object unregistering.
     */
    public static void unregister(final Object key) {
        DISPATCHER.unregister(key);
    }


    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    @Override
    public void fire() {
        log.info("Fire test event: {}", action);
        DISPATCHER.fire(this);
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
