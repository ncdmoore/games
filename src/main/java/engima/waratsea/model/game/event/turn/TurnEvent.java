package engima.waratsea.model.game.event.turn;

import engima.waratsea.model.game.event.Event;
import engima.waratsea.model.game.event.EventDispatcher;
import engima.waratsea.model.game.event.EventHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Indicates that a turn event has occurred in the game.
 */
@Slf4j
public class TurnEvent extends Event {
    private static final EventDispatcher<TurnEvent> DISPATCHER = new EventDispatcher<>("TurnEvent");

    /**
     *
     * Initialize the turn event class. This method clears out all turn event handlers.
     */
    public static void init() {
        DISPATCHER.clear();
    }

    /**
     * This is how event handlers register to receive turn event notifications.
     *
     * @param handler The handler of the turn event.
     * @param turnEventHandler The turn event handler that is registered.
     */
    public static void register(final Object handler, final EventHandler<TurnEvent> turnEventHandler) {
        DISPATCHER.register(handler, turnEventHandler);
    }

    /**
     * This is how event handlers register to receive turn event notifications.
     *
     * @param handler The handler of the turn event.
     * @param turnEventHandler The turn event handler that is registered.
     * @param keep Indicates if the event handler should always persist.
     */
    public static void register(final Object handler, final EventHandler<TurnEvent> turnEventHandler, final boolean keep) {
        DISPATCHER.register(handler, turnEventHandler, keep);
    }

    /**
     * This is how event handlers unregister for turn event notifications.
     *
     * @param handler The turn event handler that is unregistered.
     */
    public static void unregister(final Object handler) {
        DISPATCHER.unregister(handler);
    }

    /**
     * The constructor.
     *
     * @param turn The value of the turn.
     */
    public TurnEvent(final int turn) {
        this.turn = turn;
    }

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    @Override
    public void fire() {
        log.info("Fire turn event: {}", turn);
        DISPATCHER.fire(this);
    }

    @Getter
    @Setter
    private int turn;

    @Getter
    @Setter
    private int value;
}
