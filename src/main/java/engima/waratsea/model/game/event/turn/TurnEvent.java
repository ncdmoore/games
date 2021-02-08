package engima.waratsea.model.game.event.turn;

import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.game.event.GameEventHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Indicates that a turn event has occurred in the game.
 */
@Slf4j
public class TurnEvent extends GameEvent {
    private static transient List<GameEventHandler<TurnEvent>> handlers = new ArrayList<>();
    private static final transient Map<Object, GameEventHandler<TurnEvent>> MAP = new HashMap<>();

    /**
     *
     * Initialize the turn event class. This method clears out all turn event handlers.
     */
    public static void init() {
        handlers.clear();
    }

    /**
     * This is how event handlers register to receive turn event notifications.
     *
     * @param handler The handler of the turn event.
     * @param turnEventHandler The turn event handler that is registered.
     */
    public static void register(final Object handler, final GameEventHandler<TurnEvent> turnEventHandler) {
        MAP.put(handler, turnEventHandler);
        handlers = add(TurnEvent.class, handlers, turnEventHandler);
    }

    /**
     * This is how event handlers unregister for random turn event notifications.
     *
     * @param handler The turn event handler that is unregistered.
     */
    public static void unregister(final Object handler) {
        if (MAP.containsKey(handler)) {
            handlers = remove(TurnEvent.class, handlers, MAP.get(handler));
        }
    }

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    public void fire() {
        log.info("Fire turn event: {}", turn);
        handlers.forEach(h -> h.notify(this));
    }

    @Getter
    @Setter
    private int turn;

    @Getter
    @Setter
    private int value;
}
