package engima.waratsea.model.base;

import engima.waratsea.model.game.Side;
import lombok.Data;

/**
 * Uniquely identifies an airfield.
 */
@Data
public class BaseId {
    private final String name;
    private final Side side;
}
