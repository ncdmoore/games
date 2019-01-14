package engima.waratsea.model.ships;

import engima.waratsea.model.game.Side;
import lombok.Getter;

/**
 * Since a given ship may at one time be on the Allied side and then later be on the Axis side, to uniquely
 * identify a ship requires both the ship name and the side. This class is used to uniquely identify ships.
 */
public class ShipId {
    @Getter
    private final String name;

    @Getter
    private final Side side;

    /**
     * Constructor.
     * @param name The name of the ship.
     * @param side The side the ship is on: ALLIED or AXIS.
     */
    public ShipId(final String name, final Side side) {
        this.name = name;
        this.side = side;
    }
}
