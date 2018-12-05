package engima.waratsea.event;


import com.google.inject.Inject;

/**
 * This is the event base class.
 */
public abstract class GameEvent {

    // Guice injects the registry. That is how it is assigned.
    // Add a suppress warnings annotation to get rid of the "never assigned" warning.
    @SuppressWarnings("unused")
    @Inject
    private GameEventRegistry registry;

    /**
     * Generate the event. Tell the game event registry that the event has occurred.
     */
    public void fire() {
        registry.fire(this);
    }
}

