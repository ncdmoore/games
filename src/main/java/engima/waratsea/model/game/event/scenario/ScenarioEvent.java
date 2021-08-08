package engima.waratsea.model.game.event.scenario;

import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.game.event.GameEventDispatcher;
import engima.waratsea.model.game.event.GameEventHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is used to communicates scenario events to the rest of the application.
 *
 * Scenario events are used by the view model to update on existing game loads.
 * These view models are singleton objects, thus the ScenarioEvent class does not
 * provide a way to clear the handlers. These singleton objects register for
 * scenario events in their constructors which are called only once. Thus, the
 * ScenarioEvent handlers cannot be cleared on game load.
 *
 * Think of ScenarioEvents as a game mechanism that are always needed. It
 * doesn't make sense to unregister for a ScenarioEvent.
 */
@Slf4j
public class ScenarioEvent extends GameEvent {
    private static final GameEventDispatcher<ScenarioEvent> DISPATCHER = new GameEventDispatcher<>("ScenarioEvent");

    /**
     * Initialize the airfield event class. This method clears out all airfield event handlers.
     */
    public static void init() {
        DISPATCHER.clear();
    }

    /**
     * This is how event handlers register to receive scenario event notifications.
     *
     * @param handler The handler of the scenario event.
     * @param scenarioEventHandler The scenario event handler that is registered.
     */
    public static void register(final Object handler, final GameEventHandler<ScenarioEvent> scenarioEventHandler) {
        DISPATCHER.register(handler, scenarioEventHandler, true);
    }

    /**
     * This is how event handlers register to receive scenario event notifications.
     *
     * @param handler The handler of the scenario event.
     * @param scenarioEventHandler The scenario event handler that is registered.
     * @param keep Indicates if the handler should be preserved.
     */
    public static void register(final Object handler, final GameEventHandler<ScenarioEvent> scenarioEventHandler, final boolean keep) {
        DISPATCHER.register(handler, scenarioEventHandler, keep);
    }

    public ScenarioEvent(final ScenarioEventTypes type) {
        this.type = type;
    }

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    @Override
    public void fire() {
        log.info("Fire scenario event: {}", type);
        DISPATCHER.fire(this);
    }

    @Getter
    @Setter
    private ScenarioEventTypes type;
}
