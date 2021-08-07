package engima.waratsea.model.game.event.airfield;

import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.game.event.GameEventDispatcher;
import engima.waratsea.model.game.event.GameEventHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Indicates an airfield event has occurred in the game.
 */
@Slf4j
public class AirfieldEvent extends GameEvent {
    private static final GameEventDispatcher<AirfieldEvent> DISPATCHER = new GameEventDispatcher<>("AirfieldEvent");

    /**
     * Initialize the airfield event class. This method clears out all airfield event handlers.
     */
    public static void init() {
        DISPATCHER.clear();
    }

    /**
     * This is how event handlers register to receive airfield event notifications.
     *
     * @param handler The object that handles the event.
     * @param airfieldEventHandler The airfield event handler that is registered.
     */
    public static void register(final Object handler, final GameEventHandler<AirfieldEvent> airfieldEventHandler) {
        DISPATCHER.register(handler, airfieldEventHandler);
    }

    /**
     * This is how event handlers unregister for airfield event notifications.
     *
     * @param handler The airfield event handler that is unregistered.
     */
    public static void unregister(final Object handler) {
        DISPATCHER.unregister(handler);
    }

    @Getter
    @Setter
    private Airfield airfield;              // The airfield that experienced the event.

    @Getter
    @Setter
    private AirfieldEventAction action;     // The action that the airfield experienced.

    @Getter
    @Setter
    private int value;

    @Getter
    @Setter
    private AssetType by;                  // The game asset ship, sub or aircraft that caused the event. The asset that did the event. Not all events have a by.

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    @Override
    public void fire() {
        String asset = Optional.ofNullable(by).map(a -> "by" + a).orElse("");
        log.info("Fire event: {} {} {}", new Object[]{airfield.getName(), action, asset});
        DISPATCHER.fire(this);
    }
}
