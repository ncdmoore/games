package engima.waratsea.event;

/**
 * This is the event interface. Classes that implement this interface are game events.
 */
public interface GameEvent {

    /**
     * Return the event action. This represents the action of event that occurred.
     * @return The event action.
     */
    String getAction();
}

