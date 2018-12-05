package engima.waratsea.event;

import com.google.inject.Inject;
import com.google.inject.assistedinject.AssistedInject;
import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;

/**
 * Indicates that a ship event has occurred in the game.
 */
public class ShipEvent extends GameEvent {
    @Getter
    @Setter
    private String action;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private String shipType;

    /**
     * The constructor of ship events.
     */
    @Inject
    @AssistedInject
    public ShipEvent() {
        shipType = "any";
        name = "any";
    }

}
