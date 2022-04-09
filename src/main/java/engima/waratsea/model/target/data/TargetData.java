package engima.waratsea.model.target.data;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.target.TargetType;
import lombok.Data;

/**
 * Represents target data that is read an written to JSON files.
 */
@Data
public class TargetData {
    private TargetType type;
    private String name;
    private Side side;
    private String location;

    /**
     * Set the target's side.
     *
     * @param targetSide The target's side.
     * @return This target data.
     */
    public TargetData setSide(final Side targetSide) {
        this.side = targetSide;
        return this;
    }
}
