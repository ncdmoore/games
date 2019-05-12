package engima.waratsea.model.minefield.zone.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the minefield data. It contains a list of all the map grids where a potential mine may be placed
 * for this minefield.
 */
public class MinefieldZoneData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private List<String> grids;
}
