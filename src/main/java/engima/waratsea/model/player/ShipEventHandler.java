package engima.waratsea.model.player;

import com.google.inject.Inject;
import engima.waratsea.event.GameEventHandler;
import engima.waratsea.event.GameEventRegistry;
import engima.waratsea.event.ShipEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles ship events for game players.
 */
@Slf4j
public class ShipEventHandler implements GameEventHandler<ShipEvent> {

    private GameEventRegistry registry;

    @Getter
    @Setter
    private Player player;

    /**
     * Constructor called by guice.
     * @param registry The game event registry. Tracks all game event handlers.
     */
    @Inject
    public ShipEventHandler(final GameEventRegistry registry) {
        this.registry = registry;
        registry.register(ShipEvent.class, this);
    }

    /**
     * This method is called to notify the event handler that an event has fired.
     *
     * @param shipEvent the fired event.
     */
    @Override
    public void notify(final ShipEvent shipEvent) {
        log.info("Ship event notification. action {}, name {} for player {}", new Object[] {shipEvent.getAction(), shipEvent.getName(), player.getSide()});
    }
}
