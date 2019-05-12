package engima.waratsea.model.minefield.zone;

import engima.waratsea.model.game.Side;
import lombok.Getter;

/**
 * To identify a minefield requires both the name and the side. This class is used to uniquely identify minefields.
 */
public class MinefieldZoneId {
    @Getter
    private final String name;

    @Getter
    private final Side side;

    /**
     * Constructor.
     *
     * @param name The name of the minefield
     * @param side The side the minefield is on: ALLIED or AXIS.
     */
    public MinefieldZoneId(final String name, final Side side) {
        this.name = name;
        this.side = side;
    }
}
