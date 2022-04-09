package engima.waratsea.model.minefield.zone.data;

import lombok.Data;

import java.util.List;

/**
 * Represents the minefield data. It contains a list of all the map grids where a potential mine may be placed
 * for this minefield.
 */
@Data
public class MinefieldZoneData {
    private String name;
    private List<String> grids;
}
