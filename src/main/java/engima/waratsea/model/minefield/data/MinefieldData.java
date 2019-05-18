package engima.waratsea.model.minefield.data;

import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the minefield data read in from a JSON file.
 */
public class MinefieldData {
    @Getter
    @Setter
    private String zone;  //name of the minefield zone.

    @Getter
    @Setter
    private int number;   //number of minefield grids in the zone.

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private List<String> activeMapRef = new ArrayList<>();
}
