package engima.waratsea.event;

import lombok.Getter;
import lombok.Setter;

/**
 * Indicates that a ship event has occurred in the game.
 */
public class ShipEvent implements GameEvent {
    @Getter
    @Setter
    private String action;

    @Getter
    @Setter
    private String name;
}
