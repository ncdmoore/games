package engima.waratsea.model.target.data;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.target.TargetType;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents target data that is read an written to JSON files.
 */
public class TargetData {
    @Getter
    @Setter
    private TargetType type;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private String location;

}
