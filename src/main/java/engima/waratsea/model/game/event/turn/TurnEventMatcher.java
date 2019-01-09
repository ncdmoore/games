package engima.waratsea.model.game.event.turn;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * This class is used to match turn events. An entity that is looking for a particular turn event can use this
 * class to detect if wanted event has occurred.
 */
public class TurnEventMatcher {
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
    public boolean match(final TurnEvent firedEvent) {
        return matchExactTurn(firedEvent)
                ||  matchGreaterThanTurn(firedEvent);
    }

    /**
     * Returns a text string indicating under what conditions the turn event is matched.
     * @return The text explanation string.
     */
    public String getExplanation() {
        String explanation = "unknown.";
        if (turn != 0) {
            explanation = "on turn " + turn + ".";
        }

        if (turnGreaterThan != 0) {
            explanation = "potentially on or after turn " + turn + ".";
        }

        return explanation;
    }

    /**
     * For matching turns exactly, determine if the fired  turn event is a match.
     *
     * @param firedEvent The fired turn event.
     * @return True if the fired event matches this event.
     */
    private boolean matchExactTurn(final TurnEvent firedEvent) {
        return  turn != 0
                && turn == firedEvent.getTurn();
    }

    /**
     * For matching turns greater than a given turn, determine if the fired  turn event is a match.
     * @param firedEvent The fired turn event.
     * @return True if the fired event matches this event.
     */
    private boolean matchGreaterThanTurn(final TurnEvent firedEvent) {
        return turnGreaterThan != 0
                && turnGreaterThan <= firedEvent.getTurn()
                && values != null && values.contains(firedEvent.getValue());
    }
}
