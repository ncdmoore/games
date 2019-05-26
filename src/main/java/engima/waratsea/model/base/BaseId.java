package engima.waratsea.model.base;

import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;

/**
 * Uniquely identifies an airfield.
 */
public class BaseId {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Side side;

    /**
     * Constructor.
     *
     * @param name The name of the airfield.
     * @param side The side of the airfield.
     */
    public BaseId(final String name, final Side side) {
        this.name = name;
        this.side = side;
    }
}
