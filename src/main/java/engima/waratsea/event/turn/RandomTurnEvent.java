package engima.waratsea.event.turn;

import engima.waratsea.event.GameEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Indicates that a random turn event has occurred in the game.
 */
@Slf4j
public class RandomTurnEvent extends GameEvent {
    private static transient List<RandomTurnEventHandler> handlers = new ArrayList<>();

    /**
     * Initialize the random turn event class. This method clears out all random turn event handlers.
     */
    public static void init() {
        handlers.clear();
    }

    /**
     * This is how event handlers register to receive random turn event notifications.
     *
     * @param randomTurnEventHandler The random turn event handler that is registered.
     */
    public static void register(final RandomTurnEventHandler randomTurnEventHandler) {
        handlers = add(RandomTurnEvent.class, handlers, randomTurnEventHandler);
    }

    /**
     * This is how event handlers unregister for random turn event notifications.
     *
     * @param randomTurnEventHandler The random turn event handler that is unregistered.
     */
    public static void unregister(final RandomTurnEventHandler randomTurnEventHandler) {
        handlers = remove(RandomTurnEvent.class, handlers, randomTurnEventHandler);
    }

    /**
     * This is how an event is fired and all the event handlers receive
     * notification of the event.
     */
    public void fire() {
        log.info("Fire random turn event: {}", turn);
        handlers.forEach(h -> h.notify(this));
    }

    @Getter
    @Setter
    private int turn;

    @Getter
    @Setter
    private int turnGreaterThan;

    @Getter
    @Setter
    private Set<Integer> values;

    /**
     * Determine if the random fired event matches this event.
     *
     * @param firedEvent The event that was fired.
     * @return True if the fired event matches this event. False otherwise.
     */
    public boolean match(final RandomTurnEvent firedEvent) {
        return matchExactTurn(firedEvent)
                ||  matchGreaterThanTurn(firedEvent);
    }

    /**
     * For matching turns exactly, determine if the fired random turn event is a match.
     *
     * @param firedEvent The fired random turn event.
     * @return True if the fired event matches this event.
     */
    private boolean matchExactTurn(final RandomTurnEvent firedEvent) {
        return  turn != 0
                && turn == firedEvent.turn
                && values.containsAll(firedEvent.values);
    }

    /**
     * For matching turns greater than a given turn, determine if the fired random turn event is a match.
     * @param firedEvent The fired random turn event.
     * @return True if the fired event matches this event.
     */
    private boolean matchGreaterThanTurn(final RandomTurnEvent firedEvent) {
        return turnGreaterThan != 0
                && turnGreaterThan <= firedEvent.turn
                &&  values.containsAll(firedEvent.values);
    }
}
