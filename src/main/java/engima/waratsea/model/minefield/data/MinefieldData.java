package engima.waratsea.model.minefield.data;

import engima.waratsea.model.game.Side;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the minefield data read in from a JSON file.
 */
@Data
public class MinefieldData {
    private String zone;  //name of the minefield zone.
    private int number;   //number of minefield grids in the zone.
    private Side side;
    private List<String> activeMapRef = new ArrayList<>();
}
